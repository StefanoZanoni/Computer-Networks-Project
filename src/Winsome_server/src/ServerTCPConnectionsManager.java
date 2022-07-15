import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class ServerTCPConnectionsManager {

    ExecutorService threadPool;
    Selector selector;
    ServerSocketChannel socketChannel;

    ServerTCPConnectionsManager(ExecutorService threadPool, String host, int port) {

        this.threadPool = threadPool;

        try {
            socketChannel = ServerSocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.bind(new InetSocketAddress(host, port));
            selector = Selector.open();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            socketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (ClosedChannelException e) {
            throw new RuntimeException(e);
        }

    }

    public void select() {

        try {
            selector.select();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Set<SelectionKey> readyKeys = selector.selectedKeys();
        Iterator<SelectionKey> iterator = readyKeys.iterator();

        while (iterator.hasNext()) {

            SelectionKey key = iterator.next();
            iterator.remove();

            try {
                if ( key.isAcceptable() ) {
                    SocketChannel client = socketChannel.accept();
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ);
                }
                else if ( key.isReadable() ) {
                    SocketChannel client = (SocketChannel) key.channel();
                    ByteBuffer clientData = ByteBuffer.allocate(Integer.BYTES);
                    client.read(clientData);
                    int requestDim = clientData.getInt();
                    clientData = ByteBuffer.allocate(requestDim);
                    int readBytes;
                    do {
                       readBytes = client.read(clientData);
                    } while (readBytes < requestDim);

                    Task task = createTask(clientData);
                    threadPool.submit(task);
                }
            } catch (IOException e) {
                key.cancel();
                try {
                    key.channel().close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

        }

    }

    private Task createTask(ByteBuffer clientData) {

        String request = StandardCharsets.UTF_8.decode(clientData).toString();
        String[] list = request.split("\\|");
        String command = list[0].toUpperCase();
        Task task = Task.valueOf(command);
        String[] temp;
        // temp is a valid array of String only if the command sent from the client has arguments
        try {
            temp = Arrays.copyOfRange(list, 1, list.length);
        } catch (ArrayIndexOutOfBoundsException b) {
            temp = null;
        }
        if (temp != null)
            task.setAttributes( Arrays.asList(temp) );

        return task;

    }

}
