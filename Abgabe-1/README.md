# Exercise 1: Java NIO SMTP Server

This project implements a reduced SMTP server for the TU Berlin Distributed Systems assignment.

The server follows the assignment requirements: Java NIO Selector, Channel-based network and file I/O, US-ASCII SMTP communication, multiple recipients, recipient validation, DATA mode, and file storage per valid recipient.

## Requirements

- Java 11 or newer
- No Maven required
- `tools/SMTPClient.jar` for the provided course client test
- Optional: `nc` / `netcat` for manual tests

## Compile

```sh
javac -d out $(find src/main/java -name "*.java")
```

## Run the Server

Default port is `2525` and default mail storage directory is `mails`.

```sh
java -cp out smtp.SMTPServer
```

With explicit port and mail directory:

```sh
java -cp out smtp.SMTPServer 2525 mails
```

## Provided SMTPClient.jar

`SMTPClient.jar` is an external test client provided by the course. Do not modify it and do not integrate it into the server implementation.

Run the provided client with:

```sh
java -jar tools/SMTPClient.jar localhost 2525
```

Alternative command:

```sh
java -cp tools/SMTPClient.jar de.tu_berlin.dos.SMTPClient localhost 2525
```

Passing the provided client is necessary, but not sufficient for full credit. Run it at least three times for the final video and test evidence.

## Manual netcat Test

Example test with two valid recipients and one invalid recipient:

```text
HELO example.com
MAIL FROM:<sender@example.com>
RCPT TO:<abc@def.edu>
RCPT TO:<invalid@example.com>
RCPT TO:<ghi@jkl.com>
DATA
Subject: Manual test

Hello from netcat.
HELP
.
QUIT
```

Expected behavior:

- `abc@def.edu` returns `250 OK`.
- `invalid@example.com` returns `550 No such user here`.
- `ghi@jkl.com` returns `250 OK`.
- After `DATA`, the line `HELP` is stored as message content until the single-dot terminator.
- One message file is written for each valid recipient only.

## Valid Recipients

- `abc@def.edu`
- `ghi@jkl.com`
- `nmo@pqr.gov`
- `stu@vwx.de`

## Message Storage

Stored emails are written under the configured mail directory, by default `mails/`.

Folder structure:

```text
mails/
├── abc@def.edu/
├── ghi@jkl.com/
├── nmo@pqr.gov/
└── stu@vwx.de/
```

Filename format:

```text
<sender>_<message_id>
```

`message_id` is a random integer from `0` to `9999`. The server retries if a random filename already exists.

## Implementation Notes

- Network I/O uses Java NIO `ServerSocketChannel`, `SocketChannel`, `Selector`, `SelectionKey`, and `ByteBuffer`.
- File I/O uses Java NIO `FileChannel`.
- SMTP commands and responses use `StandardCharsets.US_ASCII`.
- Each client has its own `ClientSession` attached to its `SelectionKey`.
- The server does not create additional client-handling threads.
- The implementation intentionally does not add P2P, Pub/Sub, replication, REST, SOAP, RMI, TLS, authentication, databases, or mail relaying.

## Known Limitations

- This is a reduced SMTP server for the assignment, not a production mail server.
- Only the required commands are implemented: `HELO`, `MAIL FROM:`, `RCPT TO:`, `DATA`, `HELP`, and `QUIT`.
- The server stores messages locally and does not forward mail.
- The server is designed for US-ASCII SMTP communication.
