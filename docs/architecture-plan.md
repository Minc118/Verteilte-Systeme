# Architecture Plan

This document describes a possible simple Java architecture. It does not implement any classes yet.

## Design Goals

- Keep the server small and understandable.
- Use Java NIO Selector for concurrent clients.
- Keep one session object per client connection.
- Separate SMTP protocol logic from storage and validation.
- Avoid unnecessary frameworks and dependencies.
- Follow a simple client/server request/reply model.
- Use TCP socket communication and parse CRLF-terminated SMTP lines from a byte stream.
- Use Channel-based I/O for both network and file operations.
- Keep course-slide concepts as explanation, not as a reason to add extra features.

## Course-Aligned Architecture Rationale

The assignment is best modeled as a simple client/server distributed system. The SMTP client opens a TCP connection to the server, sends protocol messages, and receives SMTP replies. This directly matches the message-passing and request/reply concepts from the course slides.

The server should be stateful per client connection because SMTP has a command sequence: greeting, `HELO`, `MAIL FROM:`, one or more `RCPT TO:`, `DATA`, message body, and `QUIT`. A `ClientSession` object attached to the `SelectionKey` keeps this state without requiring one thread per client.

The communication is TCP socket based. TCP provides an ordered byte stream, so the implementation must parse SMTP command lines using CRLF boundaries and must not assume that one read operation equals one command.

Java NIO Selector is required because the assignment explicitly forbids additional client-handling threads. One selector loop can monitor many channels and react to `OP_ACCEPT` and `OP_READ` events.

This architecture is intentionally not:

- peer-to-peer, because clients and server have different roles.
- publish/subscribe, because SMTP here is direct command/reply communication.
- replicated, because mail storage is local assignment storage, not replicated distributed storage.
- microservice-based, because the assignment is a small single-server exercise.
- REST, SOAP, or RMI based, because SMTP commands over TCP are the required protocol.
- security/authentication based, because TLS, PKI, Kerberos, and authentication are not required.
- database-backed, because files in recipient directories are the required storage model.

## Possible Classes

### SMTPServer

Responsibilities:

- Read `ServerConfig`.
- Open `ServerSocketChannel`.
- Configure non-blocking mode.
- Create and run the main `Selector` loop.
- Register `OP_ACCEPT` for the server channel.
- Accept new `SocketChannel` connections.
- Configure client channels as non-blocking.
- Attach a new `ClientSession` to each client `SelectionKey`.
- Dispatch readable client channels to their session.
- Write SMTP responses through Java NIO Channels.
- Close client channels after `QUIT` or unrecoverable disconnects.

Dependencies:

- `ServerConfig`
- `ClientSession`
- `SMTPResponse`

### ClientSession

Responsibilities:

- Hold per-client state.
- Store the current `SMTPState`.
- Track sender from `MAIL FROM:`.
- Track all valid recipients from `RCPT TO:`.
- Buffer partial input lines.
- Collect message body lines while in `DATA` mode.
- Reset transaction state after a message is stored.
- Decide when the connection should close after `QUIT`.
- Keep accepted recipients even if a later recipient is invalid.
- Treat `HELP` as a command only outside `DATA` mode.

Dependencies:

- `SMTPState`
- `SMTPCommandParser`
- `RecipientValidator`
- `MailStorageService`
- `SMTPResponse`

### SMTPState

Responsibilities:

- Represent the current state of an SMTP session.
- Keep the state machine simple and explicit.

Possible states:

- `CONNECTED`
- `GREETED`
- `MAIL_STARTED`
- `RECIPIENTS_ADDED`
- `DATA_MODE`
- `MESSAGE_READY`
- `QUIT`

Dependencies:

- None.

### SMTPCommandParser

Responsibilities:

- Parse one US-ASCII command line.
- Detect supported commands:
  - `HELO`
  - `MAIL FROM:`
  - `RCPT TO:`
  - `DATA`
  - `HELP`
  - `QUIT`
- Extract sender address.
- Extract recipient address.
- Reject malformed commands.
- Keep parsing rules close to RFC 821 requirements.
- Preserve case where relevant inside mailbox arguments.
- Return structured command data instead of directly modifying session state.

Dependencies:

- Possibly a small command result object or enum.

### SMTPResponse

Responsibilities:

- Represent SMTP reply codes and text.
- Format responses as US-ASCII lines.
- Ensure each response line uses correct line ending.
- Provide helper responses such as:
  - greeting
  - `250 OK`
  - `354 Start mail input; end with <CRLF>.<CRLF>`
  - `550` invalid recipient
  - `214` help text
  - `221` quit response
  - help text
  - quit response
  - syntax error response
- Ensure every response ends with CRLF.

Dependencies:

- None.

### RecipientValidator

Responsibilities:

- Store the fixed set of valid recipients:
  - `abc@def.edu`
  - `ghi@jkl.com`
  - `nmo@pqr.gov`
  - `stu@vwx.de`
- Validate recipient addresses exactly as required.
- Keep recipient logic separate from command parsing.
- Return only true/false validation; do not perform storage or response formatting.

Dependencies:

- None.

### MailStorageService

Responsibilities:

- Create one directory per valid recipient.
- Generate random `message_id` from 0 to 9999.
- Write one file per valid recipient.
- Use filename format `<sender>_<message_id>`.
- Use Java NIO Channels for file I/O.
- Avoid storing messages for invalid recipients.
- Keep random IDs in the inclusive range 0 to 9999.
- Handle random filename collision by retrying or documenting a clear storage failure.
- Never switch to sequential message IDs.

Dependencies:

- `ServerConfig`
- Possibly `ClientSession` transaction data or a small mail message object.

### ServerConfig

Responsibilities:

- Store server port.
- Store mail output directory.
- Store buffer size.
- Store charset constant as US-ASCII.
- Keep defaults in one place.
- Store server domain/name used in greeting and `QUIT` replies.
- Store the fixed recipient list if the group prefers central configuration.

Dependencies:

- None.

## Dependency Direction

Suggested dependency flow:

```text
SMTPServer
  -> ServerConfig
  -> ClientSession
      -> SMTPCommandParser
      -> RecipientValidator
      -> MailStorageService
      -> SMTPResponse
      -> SMTPState
```

## Typical Runtime Flow

1. `SMTPServer` opens server channel and selector.
2. `OP_ACCEPT` accepts a new client.
3. A new `ClientSession` is attached to the client key.
4. `OP_READ` reads bytes from the client channel.
5. `ClientSession` decodes US-ASCII lines.
6. `SMTPCommandParser` parses commands.
7. `ClientSession` updates `SMTPState`.
8. `SMTPResponse` formats replies.
9. After `DATA` is complete, `MailStorageService` writes files with Java NIO Channels.
10. `QUIT` marks the session for closing after the final response.

## DATA Mode Flow

1. Client sends `DATA`.
2. Server validates that sender and at least one valid recipient exist.
3. Server replies with `354 Start mail input; end with <CRLF>.<CRLF>`.
4. Session switches to `DATA_MODE`.
5. Every following line is appended to the message body, including a line that says `HELP`.
6. A line containing only `.` ends `DATA_MODE`.
7. `MailStorageService` writes one file per valid recipient.
8. Server replies `250 OK` and resets the transaction state for another message.

## I/O Boundaries

- Network input: `SocketChannel.read(ByteBuffer)`.
- Network output: `SocketChannel.write(ByteBuffer)`.
- File output: `FileChannel.write(ByteBuffer)` or equivalent NIO Channel-based file writing.
- Encoding: `StandardCharsets.US_ASCII` for SMTP commands and replies.
- Line handling: accumulate bytes until CRLF is available.
- Threading: no per-client worker threads.

## Explicit Non-Goals

- No `Thread` or `ExecutorService` per client.
- No `InputStream`, `OutputStream`, `Reader`, or `Writer` for assignment-relevant network/file I/O.
- No P2P, Pub/Sub, replication, distributed storage, broker, queue, load balancer, or service registry.
- No REST, SOAP, RMI, SOA, microservice, event-driven architecture, or Agentic AI layer.
- No TLS, PKI, Kerberos, authentication, authorization, or encryption.
- No database, ORM, mail relay, DNS lookup, or real external email delivery.

## Notes for Later Implementation

- Do not add these classes until implementation starts.
- Keep the first version minimal.
- Add comments around selector setup, state transitions, and storage.
- Do not introduce Maven unless the group decides it is useful.
