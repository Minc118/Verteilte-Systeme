# Scrum Backlog

This backlog is intentionally small and suitable for a short university group assignment.

## Epics

| Epic | Priority | Effort | Suggested Role |
| --- | --- | --- | --- |
| Repository and project setup | Must | S | Coordinator |
| Java NIO server core | Must | L | NIO developer |
| SMTP protocol state machine | Must | L | Protocol developer |
| Recipient validation | Must | M | Protocol developer |
| Message storage | Must | M | Storage developer |
| Testing with SMTPClient.jar and netcat | Must | M | Test lead |
| README and video submission | Must | M | Documentation and video lead |

## User Stories

| ID | Story | Priority | Effort | Acceptance Criteria |
| --- | --- | --- | --- | --- |
| US-01 | As a group member, I want a clear project structure so that work can be migrated later to the official TU GitLab repository. | Must | S | Folder structure exists, docs exist, local git is initialized. |
| US-02 | As a tester, I want to run the provided `SMTPClient.jar` so that we can check compatibility with the course client. | Must | S | README and test plan contain both supported client commands. |
| US-03 | As an SMTP client, I want to connect to the server and receive a greeting. | Must | M | Server accepts TCP connection through Java NIO and sends SMTP greeting. |
| US-04 | As an SMTP client, I want to send `HELO`, `MAIL FROM:`, `RCPT TO:`, `DATA`, `HELP`, and `QUIT`. | Must | L | Commands are parsed with correct states and reply codes. |
| US-05 | As a sender, I want multiple valid recipients to receive the same message. | Must | M | One stored file is created per valid recipient. |
| US-06 | As the server, I want invalid recipients rejected. | Must | M | Invalid recipient returns `550` and no file is written. |
| US-07 | As a grader, I want evidence that Java NIO Selector is used. | Must | M | Code contains selector loop and no per-client threads. |
| US-08 | As a group, we want a concise video demonstration. | Must | M | Video under 10 minutes covers client tests, netcat tests, storage, Selector, and submission notes. |
| US-09 | As a protocol tester, I want `HELP` to work in normal mode and behave correctly after `DATA`. | Must | M | Normal `HELP` returns help response; `HELP` inside DATA is stored as body text. |
| US-10 | As a grader, I want message files to use random IDs. | Must | S | Filenames use `<sender>_<message_id>` and IDs are random integers from 0 to 9999. |
| US-11 | As a group member, I want the implementation to stay aligned with course scope. | Should | S | No P2P, Pub/Sub, replication, REST, SOAP, RMI, security, database, or extra framework is introduced. |

## Technical Tasks

| ID | Task | Epic | Priority | Effort | Suggested Role |
| --- | --- | --- | --- | --- | --- |
| T-01 | Create project folders and planning docs. | Repository and project setup | Must | S | Coordinator |
| T-02 | Copy `SMTPClient.jar` into `tools/` if available. | Repository and project setup | Must | S | Coordinator |
| T-03 | Decide simple source layout before implementation. | Java NIO server core | Must | S | NIO developer |
| T-04 | Implement selector setup and `OP_ACCEPT`. | Java NIO server core | Must | M | NIO developer |
| T-05 | Implement non-blocking `OP_READ` line handling. | Java NIO server core | Must | L | NIO developer |
| T-06 | Implement session state object per client. | SMTP protocol state machine | Must | M | Protocol developer |
| T-07 | Implement RFC 821 command parsing and responses. | SMTP protocol state machine | Must | L | Protocol developer |
| T-08 | Implement valid recipient set. | Recipient validation | Must | S | Protocol developer |
| T-09 | Implement message storage with Java NIO Channels. | Message storage | Must | M | Storage developer |
| T-10 | Add focused comments for important implementation parts. | Java NIO server core | Should | S | All developers |
| T-11 | Run `SMTPClient.jar` at least three times. | Testing with SMTPClient.jar and netcat | Must | S | Test lead |
| T-12 | Run manual netcat tests for multiple and invalid recipients. | Testing with SMTPClient.jar and netcat | Must | M | Test lead |
| T-13 | Verify file contents and message IDs. | Testing with SMTPClient.jar and netcat | Must | S | Test lead |
| T-14 | Finalize README commands and limitations. | README and video submission | Must | M | Documentation lead |
| T-15 | Record and submit video through ISIS. | README and video submission | Must | M | Video lead |
| T-16 | Verify `HELP` in normal command mode. | SMTP protocol state machine | Must | S | Protocol developer |
| T-17 | Verify `HELP` directly after `DATA` is stored as message content. | SMTP protocol state machine | Must | S | Test lead |
| T-18 | Review code for old Stream-based network/file I/O usage. | Java NIO server core | Must | S | NIO developer |
| T-19 | Review code for additional client-handling threads. | Java NIO server core | Must | S | Coordinator |
| T-20 | Document source priority and out-of-scope lecture topics. | README and video submission | Should | S | Documentation lead |
| T-21 | Prepare video script showing every grading-critical behavior. | README and video submission | Must | M | Video lead |
| T-22 | Verify video behavior is traceable to source code. | README and video submission | Must | S | Coordinator |

## Acceptance Criteria

- The server compiles and runs with the documented command.
- The server uses Java NIO `Selector` and Channels.
- No additional client-handling threads are created.
- Required SMTP commands work with RFC 821 compatible syntax and reply codes.
- Valid recipients receive `250 OK`.
- Invalid recipients receive `550`.
- Multiple valid recipients receive separate message files.
- Message files use `<sender>_<message_id>` with `message_id` from 0 to 9999.
- `message_id` generation is random, not sequential.
- `SMTPClient.jar` succeeds at least three times.
- Manual `netcat` tests pass.
- `HELP` in normal command mode returns a help response.
- `HELP` directly after `DATA` is treated as message body until the single-dot terminator.
- Invalid recipients never receive stored message files.
- All network I/O uses Java NIO Channels.
- All file I/O uses Java NIO Channels.
- No old Stream-based I/O is used for assignment-relevant I/O.
- README is complete.
- Video is under 10 minutes and submitted through ISIS.
- Video demonstrates the empty mail folder, changed folder, opened email content, reset folder, netcat test, `HELP` after `DATA`, and Selector code location.

## Dependencies

- Official TU GitLab repository assignment is needed before final push.
- `SMTPClient.jar` is needed for provided client tests.
- Final implementation details depend on exact RFC 821 syntax decisions.
- Video recording depends on a working server and final README.
- RFC 821 is needed for command syntax, reply codes, and sequencing details.
- Official lecture slides provide conceptual background but do not create extra implementation requirements.

## Priority Scale

- Must: required for grading or assignment compliance.
- Should: important for quality but can be simplified if time is short.
- Could: useful only if all Must and Should items are complete.

## Effort Scale

- S: small, less than half a day.
- M: medium, about half a day to one day.
- L: large, likely needs more than one day or careful review.

## Suggested 5-Person Roles

- Coordinator: repository setup, task tracking, migration to official GitLab.
- NIO developer: selector loop, channels, buffers, connection handling.
- Protocol developer: SMTP states, command parser, reply codes, recipient validation.
- Storage developer: message files, recipient directories, collision handling.
- Test and documentation lead: `SMTPClient.jar`, `netcat`, README, video checklist.

For a 5-person group, split responsibilities early:

- Person 1, Coordinator: GitLab migration, task tracking, final scope control, video traceability check.
- Person 2, NIO developer: `Selector`, `ServerSocketChannel`, `SocketChannel`, `SelectionKey`, buffers.
- Person 3, Protocol developer: SMTP parser, state machine, reply codes, `HELP`, `DATA`.
- Person 4, Storage developer: recipient directories, `FileChannel`, random IDs, storage verification.
- Person 5, Test/video lead: provided client runs, `netcat` scripts, README command verification, ISIS video.

## Definition of Done

- Code compiles from a clean checkout.
- README commands were tested.
- Required SMTP commands were tested.
- Provided client was run at least three times successfully.
- Manual netcat tests cover valid and invalid recipients.
- Stored message files were inspected manually.
- No old Stream-based I/O is used.
- Important implementation parts have concise comments.
- Video checklist is complete.
- Changes are committed and pushed after official repository exists.
- Video is submitted through ISIS before the deadline.
- Implementation remains a simple client/server SMTP server and does not add out-of-scope lecture topics.

## Grading Requirement Coverage

| Requirement | Backlog/Test/Video Coverage |
| --- | --- |
| Required commands: `HELO`, `MAIL FROM:`, `RCPT TO:`, `DATA`, `HELP`, `QUIT` | US-04, T-07, test plan command tests |
| RFC 821 syntax and reply codes | US-04, T-07, acceptance criteria |
| Multiple `RCPT TO` lines | US-05, T-12, manual `netcat` test |
| Valid recipients return `250 OK` | US-06, T-08, test plan recipient tests |
| Invalid recipients return `550` | US-06, T-12, test plan invalid-recipient tests |
| One file per valid recipient after `DATA` | US-05, T-09, T-13 |
| Random `message_id` from 0 to 9999 | US-10, T-13 |
| Java NIO Selector, no per-client threads | US-07, T-04, T-19, video code walkthrough |
| Channel-based network and file I/O | T-04, T-05, T-09, T-18 |
| US-ASCII encoding | T-07, test plan encoding checks |
| Provided `SMTPClient.jar` succeeds three times | US-02, T-11, video checklist |
| Manual `netcat` three-recipient test | T-12, video checklist |
| `HELP` directly after `DATA` | US-09, T-17, video checklist |
| README exists and commands work | T-14, test plan README checks |
| Video under 10 minutes and submitted via ISIS | US-08, T-15, T-21 |

## Sprint Plan Until 19 May

### Sprint 0: Local Preparation

- Create local planning workspace.
- Collect assignment requirements.
- Prepare README, backlog, architecture plan, test plan, and video checklist.
- Add official lecture-slide context without expanding implementation scope.

### Sprint 1: Minimal Server Skeleton

- Add basic source layout.
- Implement Java NIO server startup.
- Implement selector loop with accept and read events.
- Add per-client session attachment.
- Send initial greeting and one simple response path.
- Confirm no additional client-handling threads are created.

### Sprint 2: SMTP Behavior

- Implement SMTP state machine.
- Implement required commands.
- Implement recipient validation.
- Implement DATA mode and message completion.
- Implement `HELP` in normal mode and DATA-mode body handling.
- Validate RFC 821 reply codes for common success and error paths.

### Sprint 3: Storage and Testing

- Implement Channel-based file storage.
- Run provided client tests.
- Run manual netcat tests.
- Fix protocol and storage issues.
- Verify random message IDs from 0 to 9999.
- Verify invalid recipients do not receive files.
- Review source for accidental old Stream-based I/O.

### Sprint 4: Submission Readiness

- Finalize README.
- Record video under 10 minutes.
- Migrate to official TU GitLab repository once available.
- Push regularly.
- Submit video through ISIS.
- Show code and video behavior match.
- Upload early enough that repository permission removal cannot block submission.
