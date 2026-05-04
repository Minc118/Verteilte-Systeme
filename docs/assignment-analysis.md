# Assignment Analysis

This document is a planning checklist for Exercise 1: Java NIO SMTP Server.

No Java implementation should be added here.

## Assignment Summary

The task is to implement a reduced SMTP server in Java using Java NIO. The server must follow RFC 821 for the required command syntax and reply codes, support multiple recipients per mail transaction, validate a fixed recipient list, and store received message bodies in per-recipient folders.

Hard requirements come from `Aufgabe 1 - SMTP Server mit Java NIO.pdf` and `Bewertungsschema.pdf`. RFC 821 is the protocol reference. `Exercise1_JavaNIO.pdf` explains the required Java NIO concepts: sockets, buffers, channels, selector-based asynchronous I/O, and US-ASCII encoding.

Current planning defaults:

- Project type: small Java command-line server.
- Default server host for tests: `localhost`.
- Port: choose during implementation and document in `README.md`.
- Mail storage directory: choose during implementation and document in `README.md`.
- Maven: optional; avoid unless it clearly simplifies compilation for the group.

## Functional Requirements

- Accept TCP connections from SMTP clients.
- Send an RFC 821 compatible SMTP greeting after a client connects, normally `220 <domain> Service ready`.
- Process SMTP as a request/reply protocol: the client sends one command, the server sends one response.
- Support several clients concurrently through Java NIO Selector.
- Support at least one complete mail transaction per connection.
- Support multiple valid recipients for one message.
- Reject invalid recipients without rejecting the whole mail transaction.
- Store one file per valid recipient after `DATA`.
- Keep the behavior simple and predictable for the provided course client.
- Allow `HELP` in normal command mode and return a help response.
- Treat `HELP` directly after `DATA` as message body text until the terminating single-dot line is received.
- Close the connection cleanly after `QUIT`.

## Technical Constraints

- Use Java NIO for all network I/O.
- Use Java NIO for all file I/O.
- Use `Selector`, `ServerSocketChannel`, `SocketChannel`, `SelectionKey`, and buffers.
- Do not create additional client-handling threads.
- Do not use old `java.io` Stream-based I/O for network or file handling.
- Use US-ASCII for SMTP communication.
- Parse TCP as a byte stream, not as pre-separated messages.
- Preserve partial lines between non-blocking reads.
- Keep dependencies minimal.
- Do not add frameworks, web servers, databases, or mail libraries.
- Decide whether Maven is useful before adding it; the simple assignment likely does not need it.

## Required SMTP Commands

- `HELO`
- `MAIL FROM:`
- `RCPT TO:`
- `DATA`
- `HELP`
- `QUIT`

Required behavior by command:

- `HELO <domain>` starts the SMTP dialogue after the server greeting and should return `250 OK` when accepted.
- `MAIL FROM:<reverse-path>` starts a new mail transaction, records the sender, resets recipients and message body for that transaction, and should return `250 OK` when accepted.
- `RCPT TO:<forward-path>` records one recipient. This command can be repeated any number of times before `DATA`.
- A valid recipient returns `250 OK`; an invalid recipient returns `550` and is not stored.
- `DATA` is accepted only after `MAIL FROM:` and at least one valid `RCPT TO:`. If accepted, return `354 Start mail input; end with <CRLF>.<CRLF>`.
- In `DATA` mode, all lines are message content until a line containing only `.` is received.
- After successful storage, return `250 OK`.
- `HELP` in normal command mode returns a `214` help response and must not break the session.
- `HELP` while in `DATA` mode is message content, not a command.
- `QUIT` returns `221` and closes the transmission channel.
- Malformed or out-of-sequence commands should receive appropriate RFC-style errors such as `500`, `501`, or `503`.

## Recipient Handling

- Implement exact recipient validation for:
  - `abc@def.edu`
  - `ghi@jkl.com`
  - `nmo@pqr.gov`
  - `stu@vwx.de`
- Return `250 OK` for valid recipients.
- Return `550` error for invalid recipients.
- Allow multiple `RCPT TO:` commands before `DATA`.
- Store messages only for valid recipients.
- Ensure invalid recipients do not create directories or files.
- Keep all valid recipients for the current transaction in the session state.
- Do not let one invalid recipient clear previously accepted valid recipients.
- The provided `SMTPClient.jar` only uses one random valid recipient, so manual `netcat` tests must cover multiple and invalid recipients.

## Message Storage

- Create one directory per recipient.
- Write one file per valid recipient.
- Use filename format `<sender>_<message_id>`.
- Generate `message_id` as a random integer from 0 to 9999.
- Store the message body received during `DATA`; RFC 821 notes that mail data can include header-like lines such as `Subject:`.
- Use Java NIO Channels for file writing.
- Use `FileChannel` or another Java NIO Channel-based file API.
- If a random `message_id` collides with an existing filename, generate another random value or fail with a documented storage error; do not switch to sequential IDs.
- The grading schema explicitly warns against sequential IDs instead of random numbers.

## Java NIO Requirements

- Use one main selector loop.
- Register server channel for `OP_ACCEPT`.
- Register client channels for `OP_READ`.
- Attach one `ClientSession` object to each `SelectionKey`.
- Use per-session read/write buffers.
- Avoid blocking operations in the selector loop as much as possible.
- Keep file writes simple and small enough for the assignment scope.
- Verify with code review that no per-client thread is created.
- Use `ServerSocketChannel` for listening sockets.
- Use `SocketChannel` for client connections.
- Use `ByteBuffer` for reading and writing bytes.
- Use `SelectionKey.isAcceptable()` for new connections.
- Use `SelectionKey.isReadable()` for incoming client data.
- Use `SelectionKey.attach(...)` to bind client state to the channel.
- Optional `OP_WRITE` can be used if response buffering is needed, but the first version can stay simple if writes are small and handled carefully.

## US-ASCII Encoding

- Use explicit `StandardCharsets.US_ASCII`.
- Decode incoming command lines as US-ASCII.
- Encode SMTP responses as US-ASCII.
- Avoid default platform charset.
- SMTP commands and replies are ASCII-based according to RFC 821.
- Treat non-ASCII message body bytes conservatively and document behavior in `README.md`.
- Do not use Java's default platform charset for commands, replies, or stored text conversion.

## README Requirements

- Add compile command.
- Add server run command.
- Document default port.
- Document how to run `SMTPClient.jar`.
- Document the alternative `java -cp` command.
- Add `netcat` examples.
- Explain where stored messages can be found.
- Document known limitations.
- Mention that `SMTPClient.jar` passing is necessary but not sufficient.
- Mention manual tests for multiple recipients, invalid recipients, `HELP` after `DATA`, and message storage.
- Mention that all network and file I/O use Java NIO Channels.

## Provided SMTPClient.jar Role

- Treat `SMTPClient.jar` as an external test tool.
- Do not modify the jar.
- Do not integrate it into the server implementation.
- Run it at least three times in the final test workflow.
- Document command:

```sh
java -jar tools/SMTPClient.jar <host> <port>
```

- Document alternative command:

```sh
java -cp tools/SMTPClient.jar de.tu_berlin.dos.SMTPClient <host> <port>
```

- Remember that passing this client is necessary but not sufficient.
- The jar manifest defines `Main-Class: de.tu_berlin.dos.SMTPClient`.
- The provided client uses `StandardCharsets.US_ASCII`.
- The provided client randomly selects one of the required valid recipients.
- The provided client randomly sends `HELP` during normal command flow.
- The provided client does not fully cover multiple recipients or invalid recipients.

## Video Requirements

- Keep video under 10 minutes.
- Show server startup.
- Show `SMTPClient.jar` test at least three times.
- Show manual `netcat` tests.
- Show stored files and content.
- Show where `Selector` is used in code.
- Explain the program flow clearly.
- Mention README and final submission.
- Submit video through ISIS.
- Use a different group member than previous homework if required.
- Show the empty mail folder structure at the beginning.
- Show the changed mail folder structure after provided-client runs.
- Open at least one stored email and show its content.
- Delete or reset the mail folder structure before the manual test.
- Show a `netcat` message with three recipients: two valid and one invalid.
- Show what happens when `HELP` comes directly after `DATA`.
- Show the code location where the Java NIO Selector approach is used.
- Explain `Selector` setup, `OP_ACCEPT`, `OP_READ`, session handling, response writing, `DATA` mode, and file storage.

## Submission Workflow

- Wait for the official TU GitLab repository to be assigned.
- Copy or migrate relevant files to the official repository.
- Push regularly after repository is available.
- Do not wait until the deadline.
- Remember that late submission is technically impossible after repository permissions are removed.
- Keep video submission separate through ISIS.
- Each group must work in the automatically assigned TU GitLab repository once it exists.
- At least one group member must have logged into `git.tu-berlin.de` so the account exists.
- Repository permissions are removed after the deadline, so a late push is technically impossible.
- Video submission is through ISIS, using the required shared link or upload process described by the course.
- The video must be below 10 minutes; if longer, only the first 10 minutes are evaluated.
- For each homework, a different group member must create and present the video.

## Relation to Course Material

This assignment is a small distributed system because client and server are separate processes that communicate over the network by message passing. `01_introduction.pdf` defines distributed systems through networked hardware/software components that coordinate by exchanging messages.

The design follows the architectural model from `02_1_DS_models.pdf`: one server process exposes a service over IPC/socket communication, and clients use that service through a defined protocol. The role assignment is client/server, not peer-to-peer.

`02_2_DS_messages.pdf` is relevant because SMTP commands and replies are messages transmitted over a TCP byte stream. The implementation must parse message boundaries from bytes using CRLF line endings and must use an external data representation: US-ASCII.

`03_1_CS.pdf` is relevant because the server is a stateful client/server system. Each client session has protocol state such as greeting status, sender, accepted recipients, and `DATA` mode. The server can become a bottleneck, so Java NIO Selector is required to support multiple concurrent clients without one thread per client.

`Exercise1_JavaNIO.pdf` is the direct implementation guide for sockets, channels, buffers, explicit character encoding, and the selector approach.

## Out-of-Scope Features

- No replication, consistency protocol, CAP behavior, replica manager, or distributed storage from `02_3_DS_replica.pdf`.
- No peer-to-peer network, publish/subscribe, broker network, notification service, or message queue from `03_2_PP.pdf`.
- No RMI, REST, SOAP, SOA, microservices, event-driven architecture, or Agentic AI from `04_Programming.pdf`.
- No TLS, encryption, PKI, Kerberos, authentication, or other security mechanism from `05_Security.pdf`.
- No database, GUI, web server, SMTP relay, DNS lookup, or real email delivery.
- No extra SMTP commands unless the assignment changes.

## Grading-Critical Points

- Correct SMTP reply codes and syntax from RFC 821.
- Java NIO Selector approach.
- No additional client-handling threads.
- Channel-based network and file I/O.
- Multiple recipients per message.
- Valid recipients receive files.
- Invalid recipients receive `550` and no stored files.
- `DATA` mode terminates correctly with a single dot line.
- `HELP` behavior is clear in normal mode and after `DATA`.
- README is complete and commands work.
- Video demonstrates all critical behavior.
- Video behavior must be traceable in the code.
- Provided `SMTPClient.jar` must run successfully at least three times.
- Manual `netcat` test must show three recipients with two valid and one invalid.
- Stored emails must use random `message_id` values from 0 to 9999.
- File storage must use Java NIO Channel-based I/O.

## Point Deduction Risks

- Using blocking Stream-based I/O.
- Creating one thread per client.
- Missing support for multiple recipients.
- Storing messages for invalid recipients.
- Wrong SMTP reply codes.
- Ignoring US-ASCII encoding.
- Missing README run instructions.
- Relying only on `SMTPClient.jar` and skipping manual tests.
- Video longer than 10 minutes.
- Submitting late or forgetting ISIS video upload.
- `HELP` not returning the expected response in normal mode.
- Treating `HELP` after `DATA` as a command instead of message content.
- Storing messages with sequential IDs instead of random IDs.
- Failing to show required behavior in the video even if the code works.
- Letting invalid recipients receive files.
- Forgetting to inspect stored email content during the video.

## Likely Implementation Pitfalls

- Incorrect command parsing for `MAIL FROM:` and `RCPT TO:`.
- Losing partial lines when reading from a non-blocking channel.
- Treating `HELP` inside `DATA` mode as a command instead of message content.
- Forgetting to reset session state after one completed message.
- Mishandling clients that disconnect early.
- Random `message_id` collision.
- Accidentally using platform default charset.
- Accidentally using `FileOutputStream` or other old stream APIs.
- Assuming one `SocketChannel.read(...)` call equals one SMTP command.
- Forgetting CRLF handling and the special `\r\n.\r\n` end marker.
- Sending responses without CRLF line endings.
- Clearing valid recipients when an invalid recipient is rejected.
- Closing the whole connection on a single invalid recipient.
- Writing one shared file instead of one file per valid recipient.
- Adding unnecessary architecture from lecture slides instead of implementing the small assignment.
