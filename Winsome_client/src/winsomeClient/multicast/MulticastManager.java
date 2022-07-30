package winsomeClient.multicast;

import winsomeClient.ClientMain;

import java.io.IOException;
import java.net.*;

public class MulticastManager implements Runnable {

    private volatile boolean shutdown = false;
    private final DatagramPacket datagramPacket;
    private final MulticastSocket multicastSocket;

    public MulticastManager(int multicastLocalPort) {

        byte[] buffer = new byte[Integer.BYTES];
        datagramPacket = new DatagramPacket(buffer, buffer.length);

        InetSocketAddress socketAddress = new InetSocketAddress(ClientMain.multicastIP, ClientMain.multicastServerPort);
        NetworkInterface networkInterface;
        try {
            networkInterface = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
        } catch (SocketException | UnknownHostException e) {
            throw new RuntimeException(e);
        }
        try {
            multicastSocket = new MulticastSocket(multicastLocalPort);
            multicastSocket.setReuseAddress(true);
            multicastSocket.joinGroup(socketAddress, networkInterface);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {

        while (!shutdown) {

            try {
                multicastSocket.receive(datagramPacket);
                System.out.println("Wallet updated");
            }
            // in case of closing
            catch (SocketException ignored) {}
            catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

    }

    public void shutdown() {
        shutdown = true;
        multicastSocket.close();
    }

}