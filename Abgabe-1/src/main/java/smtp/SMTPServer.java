package smtp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public final class SMTPServer {
    private final ServerConfig config;
    private final RecipientValidator recipientValidator = new RecipientValidator();
    private final MailStorageService storageService;

    private SMTPServer(ServerConfig config) {
        this.config = config;
        this.storageService = new MailStorageService(config.mailRoot());
    }

    public static void main(String[] args) {
        try {
            ServerConfig config = ServerConfig.fromArgs(args);
            new SMTPServer(config).run();
        } catch (IllegalArgumentException ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        } catch (IOException ex) {
            System.err.println("Server error: " + ex.getMessage());
            System.exit(1);
        }
    }

    private void run() throws IOException {
        // The selector is the central NIO mechanism that lets one server loop handle many clients.
        Selector selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();

        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(config.port()));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("SMTP server listening on port " + config.port());
        System.out.println("Mail storage directory: " + config.mailRoot().toAbsolutePath());

        while (true) {
            selector.select();
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                if (!key.isValid()) {
                    continue;
                }

                try {
                    if (key.isAcceptable()) {
                        acceptClient(selector, key);
                    }
                    if (key.isValid() && key.isReadable()) {
                        readClient(key);
                    }
                    if (key.isValid() && key.isWritable()) {
                        writeClient(key);
                    }
                    if (key.isValid()) {
                        updateInterestOps(key);
                    }
                } catch (IOException ex) {
                    closeKey(key);
                }
            }
        }
    }

    private void acceptClient(Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        if (clientChannel == null) {
            return;
        }

        clientChannel.configureBlocking(false);
        SelectionKey clientKey = clientChannel.register(selector, SelectionKey.OP_READ);
        ClientSession session = new ClientSession(config, recipientValidator, storageService);
        // Each connection gets its own SMTP state without creating a client-handling thread.
        clientKey.attach(session);
        updateInterestOps(clientKey);
    }

    private void readClient(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ClientSession session = (ClientSession) key.attachment();
        if (!session.readFrom(channel)) {
            closeKey(key);
        }
    }

    private void writeClient(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ClientSession session = (ClientSession) key.attachment();
        session.writeTo(channel);
        if (session.shouldCloseNow()) {
            closeKey(key);
        }
    }

    private void updateInterestOps(SelectionKey key) {
        ClientSession session = (ClientSession) key.attachment();
        if (session == null) {
            return;
        }

        int interests = session.shouldCloseNow() ? 0 : SelectionKey.OP_READ;
        if (session.hasPendingWrites()) {
            interests |= SelectionKey.OP_WRITE;
        }
        key.interestOps(interests);
    }

    private void closeKey(SelectionKey key) {
        try {
            key.channel().close();
        } catch (IOException ignored) {
            // Closing a broken client connection should not stop the server.
        }
        key.cancel();
    }
}
