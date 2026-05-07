package smtp;

final class SMTPResponse {
    private static final String CRLF = "\r\n";

    private SMTPResponse() {
    }

    static String line(int code, String text) {
        return code + " " + text + CRLF;
    }

    static String greeting(String serverName) {
        return line(220, serverName + " Simple Mail Transfer Service Ready");
    }

    static String ok() {
        return line(250, "OK");
    }

    static String dataStart() {
        return line(354, "Start mail input; end with <CRLF>.<CRLF>");
    }

    static String help() {
        return line(214, "Commands: HELO MAIL FROM: RCPT TO: DATA HELP QUIT");
    }

    static String quit(String serverName) {
        return line(221, serverName + " Service closing transmission channel");
    }

    static String syntaxError() {
        return line(500, "Syntax error, command unrecognized");
    }

    static String parameterError() {
        return line(501, "Syntax error in parameters or arguments");
    }

    static String badSequence() {
        return line(503, "Bad sequence of commands");
    }

    static String invalidRecipient() {
        return line(550, "No such user here");
    }

    static String storageError() {
        return line(451, "Requested action aborted: local error in processing");
    }
}
