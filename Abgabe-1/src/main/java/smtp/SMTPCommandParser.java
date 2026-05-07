package smtp;

import java.util.Locale;

final class SMTPCommandParser {
    enum Type {
        HELO,
        MAIL_FROM,
        RCPT_TO,
        DATA,
        HELP,
        QUIT,
        UNKNOWN
    }

    static final class Command {
        private final Type type;
        private final String argument;

        private Command(Type type, String argument) {
            this.type = type;
            this.argument = argument;
        }

        Type type() {
            return type;
        }

        String argument() {
            return argument;
        }
    }

    Command parse(String line) {
        String trimmed = line.trim();
        String upper = trimmed.toUpperCase(Locale.ROOT);

        if (upper.startsWith("HELO")) {
            return parseHelo(trimmed);
        }
        if (upper.startsWith("MAIL FROM:")) {
            return new Command(Type.MAIL_FROM, extractPath(trimmed.substring("MAIL FROM:".length())));
        }
        if (upper.startsWith("RCPT TO:")) {
            return new Command(Type.RCPT_TO, extractPath(trimmed.substring("RCPT TO:".length())));
        }
        if (upper.equals("DATA")) {
            return new Command(Type.DATA, "");
        }
        if (upper.equals("HELP") || upper.startsWith("HELP ")) {
            return new Command(Type.HELP, trimmed.length() > 4 ? trimmed.substring(4).trim() : "");
        }
        if (upper.equals("QUIT")) {
            return new Command(Type.QUIT, "");
        }

        return new Command(Type.UNKNOWN, trimmed);
    }

    private Command parseHelo(String line) {
        if (line.length() <= 4 || !Character.isWhitespace(line.charAt(4))) {
            return new Command(Type.HELO, "");
        }
        return new Command(Type.HELO, line.substring(5).trim());
    }

    private String extractPath(String raw) {
        String value = raw.trim();
        if (value.startsWith("<") && value.endsWith(">") && value.length() >= 2) {
            value = value.substring(1, value.length() - 1).trim();
        }
        return value;
    }
}
