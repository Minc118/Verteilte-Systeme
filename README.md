# Exercise 1: Java NIO SMTP Server

This is a local preparation workspace for the TU Berlin Distributed Systems group assignment.

The official TU GitLab repository has not been assigned yet. The files in this folder are planning and documentation files that should later be copied or migrated to the official repository.

No Java server implementation exists yet.

## Provided Test Client

`SMTPClient.jar` is an external SMTP test client provided by the course. It is not part of the server implementation and should not be modified.

Run the provided client with:

```sh
java -jar tools/SMTPClient.jar <host> <port>
```

Alternative command:

```sh
java -cp tools/SMTPClient.jar de.tu_berlin.dos.SMTPClient <host> <port>
```

Passing the provided client is necessary, but not sufficient for full credit.

Manual `netcat` tests are still required, especially for:

- multiple recipients
- invalid recipients
- `HELP` directly after `DATA`
- message storage verification

## Later README Content

Before submission, this README must be extended with:

- how to compile the server
- how to run the server
- which port is used
- how to test with `SMTPClient.jar`
- how to test with `netcat`
- where emails are stored
- known limitations

