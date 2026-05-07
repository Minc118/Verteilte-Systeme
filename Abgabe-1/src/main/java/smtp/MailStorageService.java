package smtp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Random;

final class MailStorageService {
    private static final int MAX_MESSAGE_ID = 9999;
    private final Path mailRoot;
    private final Random random = new Random();

    MailStorageService(Path mailRoot) {
        this.mailRoot = mailRoot;
    }

    void store(String sender, List<String> recipients, String messageBody) throws IOException {
        // The assignment requires one stored message file per accepted recipient.
        Files.createDirectories(mailRoot);
        int messageId = chooseMessageId(sender, recipients);
        byte[] bytes = messageBody.getBytes(ServerConfig.SMTP_CHARSET);

        for (String recipient : recipients) {
            Path recipientDirectory = mailRoot.resolve(recipient);
            Files.createDirectories(recipientDirectory);
            Path target = recipientDirectory.resolve(safeFilePart(sender) + "_" + messageId);
            writeFile(target, bytes);
        }
    }

    private int chooseMessageId(String sender, List<String> recipients) throws IOException {
        // IDs must be random in 0..9999; collisions are handled by retrying.
        for (int attempt = 0; attempt < 10000; attempt++) {
            int candidate = random.nextInt(MAX_MESSAGE_ID + 1);
            if (!existsForAnyRecipient(sender, recipients, candidate)) {
                return candidate;
            }
        }
        throw new IOException("Could not find unused random message id");
    }

    private boolean existsForAnyRecipient(String sender, List<String> recipients, int messageId) {
        String fileName = safeFilePart(sender) + "_" + messageId;
        for (String recipient : recipients) {
            if (Files.exists(mailRoot.resolve(recipient).resolve(fileName))) {
                return true;
            }
        }
        return false;
    }

    private void writeFile(Path target, byte[] bytes) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        try (FileChannel channel = FileChannel.open(target, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }
        }
    }

    private String safeFilePart(String value) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (Character.isLetterOrDigit(ch) || ch == '@' || ch == '.' || ch == '-' || ch == '_') {
                builder.append(ch);
            } else {
                builder.append('_');
            }
        }
        return builder.length() == 0 ? "unknown" : builder.toString();
    }
}
