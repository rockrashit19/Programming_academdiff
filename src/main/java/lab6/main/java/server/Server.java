package lab6.main.java.server;

import lab6.main.java.collection.CollectionLoader;
import lab6.main.java.collection.CollectionManager;
import lab6.main.java.data.LabWork;
import lab6.main.java.exception.FileLoadingException;
import lab6.main.java.network.CommandRequest;
import lab6.main.java.network.CommandResponse;
import lab6.main.java.util.OutputManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.List;

public class Server {
    private final int port;
    private final CollectionManager collectionManager;
    private final OutputManager outputManager;
    private final CommandManager commandManager;
    private DatagramChannel channel;
    private Selector selector;

    public Server(int port, String filePath) {
        this.port = port;
        this.outputManager = new OutputManager(System.out);
        this.collectionManager = new CollectionManager();
        this.commandManager = new CommandManager(collectionManager, outputManager);
        loadInitialCollection(filePath);
    }

    private void loadInitialCollection(String filePath) {
        CollectionLoader loader = new CollectionLoader(filePath, outputManager);
        try {
            List<LabWork> data = loader.loadCollection();
            collectionManager.loadData(data);
        } catch (FileLoadingException e) {
            outputManager.println("Failed to load collection: " + e.getMessage());
        }
    }

    public void start() {
        try {
            channel = DatagramChannel.open();
            channel.configureBlocking(false);
            channel.bind(new InetSocketAddress(port));
            selector = Selector.open();
            channel.register(selector, SelectionKey.OP_READ);
            outputManager.println("Server started on port " + port);

            while (true) {
                selector.select();
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();
                    if (key.isReadable()) {
                        handleRequest(key);
                    }
                }
            }
        } catch (Exception e) {
            outputManager.println("Server error: " + e.getMessage());
            stop();
        }
    }

    private void handleRequest(SelectionKey key) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(65536);
            InetSocketAddress clientAddress = (InetSocketAddress) channel.receive(buffer);
            buffer.flip();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);

            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            CommandRequest request = (CommandRequest) ois.readObject();

            CommandResponse response = commandManager.executeCommand(request);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(response);
            oos.flush();

            ByteBuffer responseBuffer = ByteBuffer.wrap(baos.toByteArray());
            channel.send(responseBuffer, clientAddress);
        } catch (Exception e) {
            outputManager.println("Error handling request: " + e.getMessage());
        }
    }

    public void stop() {
        try {
            commandManager.executeSaveCommand();
            outputManager.println("Collection saved on server shutdown.");
            if (channel != null) channel.close();
            if (selector != null) selector.close();
        } catch (java.io.IOException e) {
            outputManager.println("Error during server shutdown: " + e.getMessage());
        }
    }
}