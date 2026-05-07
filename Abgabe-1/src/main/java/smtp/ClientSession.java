package smtp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

final class ClientSession {
    private final ServerConfig config;
    private final RecipientValidator recipientValidator;
    private final MailStorageService storageService;
    private final SMTPCommandParser parser = new SMTPCommandParser();
    private final ByteBuffer readBuffer;
    private final StringBuilder currentLine = new StringBuilder();
    private final StringBuilder messageBody = new StringBuilder();
    private final List<String> recipients = new ArrayList<String>();
    private final Queue<ByteBuffer> pendingWrites = new ArrayDeque<ByteBuffer>();

    private SMTPState state = SMTPState.CONNECTED;
    private String sender;
    private boolean closeAfterWrite;

    ClientSession(ServerConfig config, RecipientValidator recipientValidator, MailStorageService storageService) {
        this.config = config;
        this.recipientValidator = recipientValidator;
        this.storageService = storageService;
        this.readBuffer = ByteBuffer.allocate(config.bufferSize());
        enqueue(SMTPResponse.greeting(config.serverName()));
    }

    boolean readFrom(SocketChannel channel) throws IOException {
        int bytesRead = channel.read(readBuffer);
        if (bytesRead == -1) {
            return false;
        }

        readBuffer.flip();
        // TCP is a byte stream, so complete SMTP lines must be reconstructed across reads.
        while (readBuffer.hasRemaining()) {
            byte value = readBuffer.get();
            if (value == '\n') {
                processLine(finishLine());
            } else {
                currentLine.append(toAsciiChar(value));
            }
        }
        readBuffer.clear();
        return true;
    }

    void writeTo(SocketChannel channel) throws IOException {
        while (!pendingWrites.isEmpty()) {
            ByteBuffer next = pendingWrites.peek();
            channel.write(next);
            if (next.hasRemaining()) {
                return;
            }
            pendingWrites.remove();
        }
    }

    boolean hasPendingWrites() {
        return !pendingWrites.isEmpty();
    }

    boolean shouldCloseNow() {
        return closeAfterWrite && pendingWrites.isEmpty();
    }

    private String finishLine() {
        int length = currentLine.length();
        if (length > 0 && currentLine.charAt(length - 1) == '\r') {
            currentLine.setLength(length - 1);
        }
        String line = currentLine.toString();
        currentLine.setLength(0);
        return line;
    }

    private char toAsciiChar(byte value) {
        int unsigned = value & 0xff;
        return unsigned <= 127 ? (char) unsigned : '?';
    }

    private void processLine(String line) {
        if (state == SMTPState.DATA_MODE) {
            // Inside DATA mode, protocol-looking words such as HELP are message body text.
            processDataLine(line);
            return;
        }

        SMTPCommandParser.Command command = parser.parse(line);
        switch (command.type()) {
            case HELO:
                handleHelo(command.argument());
                break;
            case MAIL_FROM:
                handleMailFrom(command.argument());
                break;
            case RCPT_TO:
                handleRcptTo(command.argument());
                break;
            case DATA:
                handleData();
                break;
            case HELP:
                enqueue(SMTPResponse.help());
                break;
            case QUIT:
                enqueue(SMTPResponse.quit(config.serverName()));
                state = SMTPState.QUIT;
                closeAfterWrite = true;
                break;
            default:
                enqueue(SMTPResponse.syntaxError());
                break;
        }
    }

    private void handleHelo(String domain) {
        if (domain == null || domain.isEmpty()) {
            enqueue(SMTPResponse.parameterError());
            return;
        }
        state = SMTPState.GREETED;
        resetTransaction();
        enqueue(SMTPResponse.ok());
    }

    private void handleMailFrom(String argument) {
        if (state != SMTPState.GREETED && state != SMTPState.MAIL_STARTED && state != SMTPState.RECIPIENTS_ADDED) {
            enqueue(SMTPResponse.badSequence());
            return;
        }
        if (argument == null || argument.isEmpty()) {
            enqueue(SMTPResponse.parameterError());
            return;
        }

        sender = argument;
        recipients.clear();
        messageBody.setLength(0);
        state = SMTPState.MAIL_STARTED;
        enqueue(SMTPResponse.ok());
    }

    private void handleRcptTo(String argument) {
        if (state != SMTPState.MAIL_STARTED && state != SMTPState.RECIPIENTS_ADDED) {
            enqueue(SMTPResponse.badSequence());
            return;
        }
        if (argument == null || argument.isEmpty()) {
            enqueue(SMTPResponse.parameterError());
            return;
        }
        if (!recipientValidator.isValid(argument)) {
            enqueue(SMTPResponse.invalidRecipient());
            return;
        }

        if (!recipients.contains(argument)) {
            recipients.add(argument);
        }
        state = SMTPState.RECIPIENTS_ADDED;
        enqueue(SMTPResponse.ok());
    }

    private void handleData() {
        if (state != SMTPState.RECIPIENTS_ADDED || recipients.isEmpty()) {
            enqueue(SMTPResponse.badSequence());
            return;
        }
        state = SMTPState.DATA_MODE;
        messageBody.setLength(0);
        enqueue(SMTPResponse.dataStart());
    }

    private void processDataLine(String line) {
        if (".".equals(line)) {
            try {
                storageService.store(sender, recipients, messageBody.toString());
                resetTransaction();
                state = SMTPState.GREETED;
                enqueue(SMTPResponse.ok());
            } catch (IOException ex) {
                resetTransaction();
                state = SMTPState.GREETED;
                enqueue(SMTPResponse.storageError());
            }
            return;
        }

        messageBody.append(line).append("\r\n");
    }

    private void resetTransaction() {
        sender = null;
        recipients.clear();
        messageBody.setLength(0);
    }

    private void enqueue(String response) {
        pendingWrites.add(ByteBuffer.wrap(response.getBytes(ServerConfig.SMTP_CHARSET)));
    }
}
