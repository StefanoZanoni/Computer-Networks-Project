package winsomeClient.multicast;

import winsomeClient.ClientMain;

import java.io.IOException;
import java.net.*;

public class MulticastManager implements Runnable {

    private volatile boolean shutdown = false;
    private final DatagramPacket datagramPacket;
    private final MulticastSocket multicastSocket;
    private final InetSocketAddress group;
    private final NetworkInterface networkInterface;

    public MulticastManager() throws IOException {

        byte[] buffer = new byte[Integer.BYTES];
        datagramPacket = new DatagramPacket(buffer, buffer.length);

        group = new InetSocketAddress(ClientMain.multicastIP, ClientMain.multicastPort);
        networkInterface = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());

        multicastSocket = new MulticastSocket(ClientMain.multicastPort);
        multicastSocket.setReuseAddress(true);
        multicastSocket.joinGroup(group, networkInterface);

    }

    @Override
    public void run() {

        while (!shutdown) {

            try {
                multicastSocket.receive(datagramPacket);
                System.out.println("< Wallet updated");
                System.out.print("> ");
            }
            // in case of closing
            catch (SocketException ignored) { return; }
            catch (IOException e) {
                e.printStackTrace(System.err);
                System.err.print("An error occurred while receiving datagram packet");
            }

        }

    }

    public void shutdown() {

        shutdown = true;

        try {
            multicastSocket.leaveGroup(group, networkInterface);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
        multicastSocket.close();

    }

}