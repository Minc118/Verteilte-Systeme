# Video Checklist

Goal: show all grading-critical behavior in under 10 minutes.

## Before Recording

- Prepare a clean terminal.
- Start from a clean or empty mail storage folder.
- Make sure the server compiles.
- Make sure `SMTPClient.jar` is in `tools/`.
- Prepare the netcat test commands.
- Prepare a short explanation of Java NIO Selector usage.
- Prepare the exact README commands for compile, server start, provided client, and `netcat`.
- Prepare a clean mail storage directory so the video can show it empty first.
- Prepare the source file location where `Selector.open()`, `OP_ACCEPT`, `OP_READ`, and `SelectionKey.attach(...)` are used.

## Suggested Timeline

| Time | Segment | Content |
| --- | --- | --- |
| 0:00-0:45 | Introduction | State assignment goal: reduced SMTP server with Java NIO. |
| 0:45-1:30 | Folder structure | Show empty or clean folder structure at the beginning. |
| 1:30-2:15 | Start server | Run the server and show the selected port. |
| 2:15-3:15 | Provided client | Run `SMTPClient.jar` at least three times without errors. |
| 3:15-4:00 | Stored files | Show changed folder structure and open at least one received email. |
| 4:00-4:30 | Reset storage | Delete or reset the mail folder structure for manual testing. |
| 4:30-6:15 | netcat test | Send one message with 3 recipients: 2 valid and 1 invalid. |
| 6:15-7:00 | Verify files | Show saved email files and their content. |
| 7:00-7:45 | HELP after DATA | Explain that `HELP` directly after `DATA` is message body until `.` ends DATA mode. |
| 7:45-8:45 | Code walkthrough | Show Java NIO Selector location and program flow. |
| 8:45-9:30 | Course context and submission | Mention distributed-system context, README, GitLab migration, and ISIS video submission. |
| 9:30-10:00 | Buffer | Keep time for small delays. |

## Required Demonstrations

- Show empty or clean folder structure at the beginning.
- Run server with the provided `SMTPClient.jar` at least three times without errors.
- Show changed folder structure.
- Open at least one received email and show its content.
- Delete or reset the mail folder structure.
- Use `netcat` or similar to connect to the server.
- Send one message with 3 recipients:
  - valid: `abc@def.edu`
  - invalid: `invalid@example.com`
  - valid: `ghi@jkl.com`
- Show valid recipients returning `250 OK`.
- Show invalid recipient returning `550`.
- Show the saved email files and their content.
- Explain what happens when `HELP` comes directly after `DATA`.
- Show the code location where Java NIO Selector is used.
- Show where Channel-based file storage is implemented.
- Show that no per-client thread handling is used.
- Mention README and final submission.
- Mention that the video must be submitted through ISIS.
- Keep the video below 10 minutes.

## Program Flow to Explain

- selector setup
- `OP_ACCEPT`
- `OP_READ`
- `ClientSession` handling
- response writing
- `DATA` mode
- file storage
- `FileChannel` storage

## Course Context Explanation

Use a short explanation only; do not turn the video into a lecture.

- This is a distributed system because SMTP client and server are separate processes that communicate over a network by message passing.
- SMTP is the protocol that defines message formats, command order, and reply codes.
- TCP gives an ordered byte stream, so the server must parse SMTP lines and the `DATA` terminator from bytes.
- US-ASCII is the external data representation used for SMTP commands and replies.
- Java NIO Selector lets one server process handle multiple client connections without creating one thread per client.
- The architecture is client/server, not peer-to-peer, not publish/subscribe, and not replicated storage.

## Grading-Critical Checklist

- Empty mail folder is visible at the beginning.
- `SMTPClient.jar` succeeds at least three times.
- Changed mail folder is visible after provided-client runs.
- At least one stored email is opened and readable.
- Mail folder is deleted or reset before manual testing.
- `netcat` sends one message with 3 recipients: 2 valid and 1 invalid.
- Valid recipients show `250 OK`.
- Invalid recipient shows `550`.
- Stored files exist only for valid recipients.
- Stored file content is shown after the `netcat` test.
- `HELP` directly after `DATA` is explained and demonstrated as message content.
- Java NIO Selector code location is shown.
- Typical flow is explained: selector setup, `OP_ACCEPT`, `OP_READ`, session handling, response writing, `DATA` mode, file storage.
- README and ISIS submission are mentioned.
- Video stays below 10 minutes.

## Out-of-Scope Mention If Asked

The implementation should not include P2P, Pub/Sub, replication, databases, REST, SOAP, RMI, TLS, PKI, Kerberos, or authentication. These topics appear in course material, but they are not required for this assignment.

## Speaking Notes

- The server uses Java NIO Channels for network I/O.
- The server uses Java NIO Channels for file I/O.
- The server does not create additional client-handling threads.
- Each client connection has its own session object.
- Multiple valid recipients are supported.
- Invalid recipients are rejected and do not receive stored files.
- The provided client passing is necessary but not sufficient.
- Manual netcat tests cover behavior that the provided client may not cover.
- The video must show behavior that can also be found in the code.
- Late submission is technically impossible after repository permissions are removed, so the final version should be pushed early.
