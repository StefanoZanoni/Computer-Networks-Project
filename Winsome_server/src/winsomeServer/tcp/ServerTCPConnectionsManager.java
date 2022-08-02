package winsomeServer.tcp;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerTCPConnectionsManager implements Closeable {

    private boolean end = false;
    private boolean closed = false;
    ExecutorService threadPool;
    Selector selector;
    ServerSocketChannel socketChannel;

    public ServerTCPConnectionsManager(ExecutorService threadPool, InetAddress host, int port) {

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

        while (true) {

            // wakeup prevents the select from block and not throw
            // ClosedSocketException if it is closed
            if (!end)
                selector.wakeup();
            else break;

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

                    if (key.isAcceptable()) {
                        SocketChannel client = socketChannel.accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                    } else if (key.isReadable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer clientData = ByteBuffer.allocate(Integer.BYTES);
                        int readBytes = 0;
                        do {
                            readBytes += client.read(clientData);
                        } while (readBytes < Integer.BYTES);
                        clientData.flip();
                        int requestDim = clientData.getInt();
                        clientData = ByteBuffer.allocate(requestDim);
                        readBytes = 0;
                        do {
                            readBytes += client.read(clientData);
                        } while (readBytes < requestDim);
                        clientData.flip();
                        Task task = createTask(clientData, client);
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

    }

    private Task createTask(ByteBuffer clientData, SocketChannel client) {

        String request = StandardCharsets.UTF_8.decode(clientData).toString();
        String[] list = request.split("\\|");
        String command = list[0];
        Task task = Task.valueOf(command);
        task.setClient(client);
        String[] arguments;

        // arguments is a valid array of String only if the command sent from the client has arguments
        try {
            arguments = Arrays.copyOfRange(list, 1, list.length);
        } catch (ArrayIndexOutOfBoundsException b) {
            arguments = null;
        }
        if (arguments != null)
            task.setAttributes( Arrays.asList(arguments) );

        return task;

    }

    public boolean isClosed() { return closed; }

    @Override
    public void close() throws IOException {

        end = true;

        threadPool.shutdownNow();
        try {
            if (threadPool.awaitTermination(5, TimeUnit.SECONDS))
                System.out.println("All executing tasks end correctly");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (socketChannel.isOpen()) {
            try {
                socketChannel.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        for (SelectionKey key : selector.keys()) {
            try {
                key.channel().close();
                key.cancel();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        selector.wakeup();
        try {
            selector.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        closed = true;

    }

}