# Test Plan

This plan covers the grading-critical behavior for the Java NIO SMTP server.

## Test Environment

Fill this table during implementation and final testing.

| Item | Value |
| --- | --- |
| Operating system | macOS local development machine |
| Java version | Java 23.0.1 during local verification |
| Server host | `localhost` for local tests |
| Server port | Default `2525`; `2526` was also used for follow-up boundary tests |
| Mail storage directory | Default `mails` |
| Command used to compile | `javac -d out $(find src/main/java -name "*.java")` |
| Command used to run | `java -cp out smtp.SMTPServer 2525 mails` |

## How to Run the Server

The README is the source of truth for compile and run commands. Current commands:

```sh
javac -d out $(find src/main/java -name "*.java")
java -cp out smtp.SMTPServer 2525 mails
```

During final verification, copy commands directly from `README.md` and confirm they work from a clean checkout.

## How to Run the Provided SMTPClient.jar

Primary command:

```sh
java -jar tools/SMTPClient.jar <host> <port>
```

Alternative command:

```sh
java -cp tools/SMTPClient.jar de.tu_berlin.dos.SMTPClient <host> <port>
```

Notes:

- `SMTPClient.jar` is an external course test tool.
- Passing it is required but not sufficient.
- The jar should not be modified.

## Provided Client Test

- Run the provided client at least three times.
- Record date, command, result, and any output.
- The course grading schema explicitly requires at least three successful runs with the provided client.
- `SMTPClient.jar` randomly chooses one valid recipient and may randomly send `HELP` during normal command flow.
- Passing this client is necessary but not sufficient.

| Run | Command | Expected Result | Actual Result |
| --- | --- | --- | --- |
| 1 | `java -jar tools/SMTPClient.jar localhost 2525` | Completes without server error. | Passed locally |
| 2 | `java -jar tools/SMTPClient.jar localhost 2525` | Completes without server error. | Passed locally, included random `HELP` |
| 3 | `java -jar tools/SMTPClient.jar localhost 2525` | Completes without server error. | Passed locally |

## Manual netcat Test

Example command:

```sh
nc <host> <port>
```

- Confirm greeting appears after connecting.
- Type commands manually.
- Verify reply codes.
- Verify stored files after `DATA`.
- Use CRLF-compatible terminal behavior where possible.
- Manual tests are required because the provided client does not cover multiple recipients and invalid recipients sufficiently.
- This is a request/reply protocol test: send a command, inspect the response code, continue with the next command.

## Test with 3 Recipients

Scenario:

- valid recipient: `abc@def.edu`
- invalid recipient: `invalid@example.com`
- valid recipient: `ghi@jkl.com`

Example session:

```text
HELO example.com
MAIL FROM:<sender@example.com>
RCPT TO:<abc@def.edu>
RCPT TO:<invalid@example.com>
RCPT TO:<ghi@jkl.com>
DATA
Subject: Manual test

Hello from netcat.
.
QUIT
```

Expected:

- Valid recipients return `250 OK`.
- Invalid recipient returns `550`.
- Message is stored only for `abc@def.edu` and `ghi@jkl.com`.
- No file is stored for `invalid@example.com`.

## Test Valid Recipient Returns 250 OK

- Send `RCPT TO:<abc@def.edu>`.
- Confirm reply is `250 OK`.
- Repeat for all required valid recipients.

Required valid recipients:

- `abc@def.edu`
- `ghi@jkl.com`
- `nmo@pqr.gov`
- `stu@vwx.de`

## Test Invalid Recipient Returns 550

- Send at least one invalid `RCPT TO:` address.
- Confirm reply starts with `550`.
- Confirm no directory or file is created for the invalid recipient.
- Confirm the session can continue after the invalid recipient if at least one valid recipient exists.
- Confirm previously accepted valid recipients are not cleared by an invalid recipient.

## Test Multiple RCPT TO Lines

- Send at least two valid `RCPT TO:` commands before `DATA`.
- Confirm each valid recipient receives a stored file.
- Confirm one message transaction can include multiple recipients.
- Include one invalid recipient between two valid recipients.
- Confirm only valid recipients receive message files.
- Confirm the message body is written once per valid recipient.

## Test DATA Mode

- Send `DATA` only after at least one valid recipient.
- Confirm server enters DATA mode.
- Send several body lines.
- End DATA with a single dot line: `.`.
- Confirm server stores files after DATA ends.
- Confirm the server returns `354 Start mail input; end with <CRLF>.<CRLF>` after accepting `DATA`.
- Confirm the server returns `250 OK` after the single-dot terminator and successful storage.
- Confirm commands sent inside `DATA` mode are treated as body lines.
- Confirm `DATA` before `MAIL FROM:` or before a valid `RCPT TO:` returns an error such as `503 Bad sequence of commands`.

## Test HELP in Normal Command Mode

- Connect to server.
- Send `HELP` before `DATA`.
- Confirm a valid help response is returned.
- Confirm session can continue afterward.
- Expected success response code: `214`.
- Run this test at several points, for example after greeting, after `HELO`, after `MAIL FROM:`, after `RCPT TO:`, and after message completion.
- The provided client may send `HELP` randomly, so this behavior is required for compatibility.

## Test HELP Directly After DATA

- Send `DATA`.
- Then send a line containing `HELP` before the terminating dot.
- Confirm `HELP` is treated as message body content while in DATA mode.
- Confirm the stored message contains the line `HELP`.
- Then run a separate test where `HELP` is sent after the DATA terminating dot and confirm it is treated as a command again.
- This is a grading-critical video item.
- The server must not return a `214` response for `HELP` while still inside `DATA` mode.
- After the single-dot terminator, `HELP` must again be parsed as a command.

## Test QUIT

- Send `QUIT`.
- Confirm quit reply is returned.
- Confirm server closes the client connection cleanly.
- Confirm server remains available for new clients.
- Expected response code: `221`.
- Confirm one client closing does not stop the server.

## Test File Storage

- Confirm one directory per valid recipient.
- Confirm one message file per valid recipient.
- Confirm file name format is `<sender>_<message_id>`.
- Open at least one stored file and verify content.
- Confirm directories are named for recipient addresses.
- Confirm invalid recipients do not get directories or files.
- Confirm stored content matches the message body sent after `DATA`.
- Confirm storage uses Java NIO Channel-based file I/O during code review.

## Test Random message_id Between 0 and 9999

- Send several messages.
- Confirm filename message IDs are numeric.
- Confirm each ID is between 0 and 9999.
- Check how implementation handles filename collision.
- Confirm IDs are not sequential counters.
- Confirm the implementation uses random generation.
- The grading schema lists sequential IDs as a point deduction risk.

## Test US-ASCII Encoding

- Confirm server uses `StandardCharsets.US_ASCII`.
- Send normal ASCII commands and body.
- Optionally send non-ASCII characters and document behavior.
- Confirm implementation does not use default platform charset.
- Confirm responses are encoded as US-ASCII.
- Confirm SMTP command parsing is based on ASCII-compatible CRLF lines.
- Confirm the code does not use `String.getBytes()` without a charset.
- Confirm the code does not use `new String(bytes)` without a charset.

## Test TCP Byte Stream Parsing

- Send multiple SMTP commands quickly in one terminal paste and confirm each command is parsed separately.
- Send a command in pieces if possible and confirm the server preserves partial input until CRLF arrives.
- Confirm one `SocketChannel.read(...)` call is not assumed to contain exactly one command.
- Confirm CRLF line endings and the single-dot `DATA` terminator are handled correctly.

## Test Multiple Clients and Non-Blocking Selector Handling

- Start the server once.
- Open two or more `netcat` clients at the same time.
- Begin a mail transaction in one client and pause before `DATA` completion.
- Use the second client to complete a separate transaction.
- Confirm the first session state is not mixed with the second.
- Confirm the server remains responsive without creating a thread per client.

## Test Selector Instead of Additional Threads

- Review source code.
- Confirm `Selector.open()` is used.
- Confirm `SelectionKey` and channel registration are used.
- Confirm `SelectionKey.attach(...)` attaches a client session.
- Confirm no per-client thread is created.
- Confirm network I/O uses Java NIO Channels.
- Confirm file I/O uses Java NIO Channels.
- Confirm `ServerSocketChannel` is configured non-blocking.
- Confirm accepted `SocketChannel` instances are configured non-blocking.
- Confirm client session objects are attached with `SelectionKey.attach(...)`.

## Test No Old Stream-Based I/O

- Search source for `InputStream`, `OutputStream`, `Reader`, `Writer`, `FileInputStream`, and `FileOutputStream`.
- Confirm assignment-relevant network I/O does not use old Stream APIs.
- Confirm assignment-relevant file I/O does not use old Stream APIs.
- If any old Stream API appears for unrelated tooling, document why it is not part of server I/O.

## Test README Commands

- Copy compile command from README and run it.
- Copy server run command from README and run it.
- Copy `SMTPClient.jar` command from README and run it.
- Copy `netcat` command from README and run it.
- Fix README if any command is outdated.
- Confirm README documents the server port.
- Confirm README documents the mail storage directory.
- Confirm README documents known limitations.
- Confirm README states that `SMTPClient.jar` passing is necessary but not sufficient.

## Course Concept Checks

- Distributed system: client and server communicate over a network by passing messages.
- Protocol: SMTP defines message format, command sequence, and reply codes.
- Client/server model: one server provides the SMTP service to multiple clients.
- TCP byte stream: parsing must recover SMTP lines from bytes.
- External data representation: SMTP uses US-ASCII.
- Concurrency: Java NIO Selector handles multiple clients without one thread per client.


## Current Local Verification Log

These checks were run after the first implementation pass:

- Compile: `javac -d out $(find src/main/java -name "*.java")` passed.
- Provided client primary command passed three times: `java -jar tools/SMTPClient.jar localhost 2525`.
- Provided client alternative command passed once: `java -cp tools/SMTPClient.jar de.tu_berlin.dos.SMTPClient localhost 2525`.
- Manual 3-recipient test passed: `abc@def.edu` and `ghi@jkl.com` returned `250 OK`; `invalid@example.com` returned `550 No such user here`.
- `HELP` directly after `DATA` was stored in the message body and did not return `214` while in DATA mode.
- Bad sequence test passed: `DATA` before a valid transaction returned `503 Bad sequence of commands`.
- Normal `HELP` test passed with `214`.
- Split TCP command test passed: `HE` and `LO split.example` sent separately were reconstructed as `HELO split.example`.
- Multiple-client test passed: two simultaneous socket clients kept independent session state.
- Source scan found required NIO classes: `Selector`, `ServerSocketChannel`, `SocketChannel`, `SelectionKey`, `ByteBuffer`, `FileChannel`, and `StandardCharsets.US_ASCII`.
- Source scan found no `Thread`, `Executor`, `InputStream`, `OutputStream`, `Reader`, `Writer`, `FileInputStream`, or `FileOutputStream` in `src/main/java`.
