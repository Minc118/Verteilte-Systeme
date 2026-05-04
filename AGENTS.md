# AGENTS.md

## Project Context

This is a university group assignment for the course Distributed Systems at TU Berlin.

The goal is to implement a reduced SMTP server using Java NIO.

The official TU GitLab repository has not been assigned yet. This local workspace is only for planning and later migration.

Once the official repository is available, all relevant files should be copied or migrated there.

## Source Priority

Use sources in this order when requirements appear to conflict:

1. `docs/source_files/Aufgabe 1 - SMTP Server mit Java NIO.pdf`
2. `docs/source_files/Bewertungsschema.pdf`
3. RFC 821: Simple Mail Transfer Protocol
4. `docs/source_files/Exercise1_JavaNIO.pdf`
5. `docs/source_files/ha_ablauf.pdf`
6. Official lecture slides in `docs/source_files/`
7. `docs/source_files/Skript_VS.pdf` as student summary only

The assignment sheet and grading schema define the hard requirements. RFC 821 defines protocol details. Lecture slides provide conceptual background and must not expand the implementation scope.

## Communication Language

- Always communicate with the user in Chinese.
- If the user writes prompts in English for precision, still answer in Chinese.
- Keep technical names, code, commands, file paths, Java class names, and SMTP protocol commands in English where appropriate.

## Hard Technical Requirements

- Use Java NIO.
- Use the Selector approach.
- Do not create additional client-handling threads.
- Use Channel-based I/O for both network and file system.
- Do not use old Stream-based I/O.
- Use US-ASCII encoding for SMTP communication.
- Prefer simple and maintainable Java.
- Avoid unnecessary frameworks and dependencies.
- Maven is optional and should only be introduced if clearly useful.

## SMTP Requirements

Implement the following SMTP commands:

- HELO
- MAIL FROM:
- RCPT TO:
- DATA
- HELP
- QUIT

Required behavior:

- Use RFC 821 for exact syntax and reply codes.
- Support multiple RCPT TO lines per message.
- Validate recipients.
- Required valid recipients:
  - abc@def.edu
  - ghi@jkl.com
  - nmo@pqr.gov
  - stu@vwx.de
- Valid recipient returns 250 OK.
- Invalid recipient returns 550 error.
- After DATA, store one message file per valid recipient.
- Folder structure: one directory per recipient.
- Filename format: <sender>_<message_id>.
- message_id must be a random integer from 0 to 9999.

## Provided Test Client

- SMTPClient.jar is the provided course test client.
- Treat SMTPClient.jar as an external test tool.
- Do not modify it.
- Do not integrate it into the server implementation.
- Document how to run it.
- Passing this client is required but not enough for full credit.
- Manual netcat tests are still required for multiple recipients, invalid recipients, HELP after DATA, and message storage verification.

## Official Course Slides Usage

Relevant slides for implementation reasoning:

- `01_introduction.pdf`: distributed system basics, message passing, protocol, network, component, heterogeneity, concurrency, and failure awareness.
- `02_1_DS_models.pdf`: system models, architectural model, IPC, socket programming, client/server roles, and communication paradigms.
- `02_2_DS_messages.pdf`: message passing, synchronous/asynchronous communication, TCP vs UDP, byte streams, external data representation, marshalling/unmarshalling, and encoding issues.
- `03_1_CS.pdf`: client/server architecture, request/reply interaction, server role, stateful vs stateless server discussion, failure semantics, and server bottleneck considerations.
- `Exercise1_JavaNIO.pdf`: Java NIO, Selector, Buffer, Channel, Socket, US-ASCII, and SMTP exercise guidance.

Background-only slides:

- `02_3_DS_replica.pdf`: do not implement replication, consistency protocols, CAP behavior, replica managers, or distributed storage.
- `03_2_PP.pdf`: do not implement peer-to-peer, publish/subscribe, broker networks, notification services, or message queues.
- `04_Programming.pdf`: do not implement RMI, REST, SOAP, SOA, microservices, event-driven architecture, or Agentic AI.
- `05_Security.pdf`: do not implement TLS, encryption, PKI, Kerberos, authentication, or other security mechanisms unless the assignment explicitly changes.
- `Skript_VS.pdf`: use only as student summary; never let it override official materials.

Do not add features just because they appear in lecture slides. Keep the implementation strictly aligned with the SMTP assignment.

## Out-of-Scope Warnings

- Do not implement extra SMTP commands beyond `HELO`, `MAIL FROM:`, `RCPT TO:`, `DATA`, `HELP`, and `QUIT` unless the assignment is updated.
- Do not add databases, web APIs, REST endpoints, GUI tools, mail relaying, authentication, encryption, or distributed storage.
- Do not replace the required Java NIO Selector design with framework servers or thread-per-client code.
- Do not modify, unpack into source, or integrate `tools/SMTPClient.jar`; it remains an external test tool.

## Design Guidelines

- Keep the implementation simple.
- Use a clear SMTP session state machine.
- Each client connection must have its own session object.
- Use SelectionKey.attach(...) to associate a session object with each client connection.
- Separate responsibilities clearly:
  - server loop
  - session state
  - command parsing
  - response formatting
  - recipient validation
  - message storage
- Avoid over-engineering.
- Avoid unnecessary abstractions.

## Testing Requirements

Manual tests must cover:

- Provided SMTPClient.jar runs successfully at least three times.
- netcat test with three recipients:
  - two valid recipients
  - one invalid recipient
- HELP in normal command mode.
- HELP directly after DATA.
- Multiple recipients.
- Invalid recipients are rejected and do not receive stored files.
- Message files are written with random message IDs.
- Mail content can be opened and verified.
- Server uses Selector, not client-handling threads.
- README commands work.

## Documentation Requirements

README.md must include:

- How to compile.
- How to run the server.
- Which port is used.
- How to test with SMTPClient.jar.
- How to test with netcat.
- Where emails are stored.
- Known limitations.

## Video and Submission Requirements

- The video must show all grading-critical behavior.
- The video must stay under 10 minutes.
- The video is submitted through ISIS.
- For each homework, a different group member must create and present the video.
- Once the official repository is available, push regularly.
- Do not wait until the deadline.
- Late submission is technically impossible after repository permissions are removed.

## Coding Style for Later Implementation

- Use clear class names.
- Add comments for important implementation parts.
- Keep methods reasonably small.
- Prefer explicit Charset US_ASCII.
- Avoid default platform charset.
- Do not silently swallow exceptions.
- Do not commit build artifacts.
