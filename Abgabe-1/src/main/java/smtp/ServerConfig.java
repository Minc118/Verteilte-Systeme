package smtp;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

final class ServerConfig {
    static final Charset SMTP_CHARSET = StandardCharsets.US_ASCII;

    private final int port;
    private final Path mailRoot;
    private final String serverName;
    private final int bufferSize;

    private ServerConfig(int port, Path mailRoot, String serverName, int bufferSize) {
        this.port = port;
        this.mailRoot = mailRoot;
        this.serverName = serverName;
        this.bufferSize = bufferSize;
    }

    static ServerConfig fromArgs(String[] args) {
        int port = 2525;
        Path mailRoot = Paths.get("mails");

        if (args.length > 2) {
            throw new IllegalArgumentException("Usage: java -cp out smtp.SMTPServer [port] [mail-directory]");
        }
        if (args.length >= 1) {
            port = parsePort(args[0]);
        }
        if (args.length == 2) {
            mailRoot = Paths.get(args[1]);
        }

        return new ServerConfig(port, mailRoot, "localhost", 8192);
    }

    private static int parsePort(String value) {
        try {
            int parsed = Integer.parseInt(value);
            if (parsed < 1 || parsed > 65535) {
                throw new IllegalArgumentException("Port must be between 1 and 65535");
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Port must be a number", ex);
        }
    }

    int port() {
        return port;
    }

    Path mailRoot() {
        return mailRoot;
    }

    String serverName() {
        return serverName;
    }

    int bufferSize() {
        return bufferSize;
    }
}
