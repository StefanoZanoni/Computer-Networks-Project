package winsomeServer.network;

import winsome.base.Post;
import winsomeServer.ServerMain;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;

public class RewardsCalculator extends TimerTask implements AutoCloseable {

    private boolean closed = false;
    /*
        int[0] : numberOfPreviousUpvotes
        int[1] : numberOfPreviousDownvotes
        int[2] : numberOfPreviousComments
        int[3] : age
    */
    private final HashMap<Post, int[]> previousStatistics = new HashMap<>();
    private final float authorEarnPercentage;
    private final MulticastSocket multicastSocket;
    private final DatagramPacket datagramPacket;
    private final InetSocketAddress group;
    private final NetworkInterface networkInterface;

    public RewardsCalculator(float authorEarnPercentage) {

        this.authorEarnPercentage = authorEarnPercentage;

        group = new InetSocketAddress(ServerMain.multicastIP, ServerMain.multicastPort);
        try {
            networkInterface = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
        } catch (SocketException | UnknownHostException e) {
            throw new RuntimeException(e);
        }

        try {
            multicastSocket = new MulticastSocket(ServerMain.multicastPort);
            multicastSocket.setReuseAddress(true);
            multicastSocket.joinGroup(group, networkInterface);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        byte[] data;
        ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES);
        data = byteBuffer.array();
        datagramPacket = new DatagramPacket(data, data.length, ServerMain.multicastIP, ServerMain.multicastPort);

    }

    @Override
    public void run() {

        if (computeReward() != 0)
            try {
                multicastSocket.send(datagramPacket);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

    }

    private float computeReward() {

        float reward = 0;

        for (Post post : SocialNetworkManager.posts.values()) {

            String author = post.getOwner();
            int[] statistics = previousStatistics.putIfAbsent( post, new int[]{0, 0, 0, 1} );
            if (statistics != null)
                reward = post.computeReward(statistics[0], statistics[1], statistics[2], statistics[3]);
            else
                reward = post.computeReward(0, 0, 0, 1);

            if (reward != 0) {

                float authorReward = reward * authorEarnPercentage;
                SocialNetworkManager.users.get(author).getWallet().addTransaction(authorReward);

                float curatorReward = reward * (1 - authorEarnPercentage);
                Map<String, Boolean> peopleWhoInteracted = new HashMap<>();
                for (String user : post.getUpvotes()) {
                    peopleWhoInteracted.putIfAbsent(user, true);
                }
                for (String user : post.getCommentsAuthors()) {
                    peopleWhoInteracted.putIfAbsent(user, true);
                }
                Set<String> people = peopleWhoInteracted.keySet();
                int peopleWhoInteractedSize = people.size();
                curatorReward /= peopleWhoInteractedSize;
                for (String user : people) {
                    SocialNetworkManager.users.get(user).getWallet().addTransaction(curatorReward);
                }

            }

            previousStatistics.get(post)[0] = post.getUpvotes().size();
            previousStatistics.get(post)[1] = post.getDownvotes().size();
            previousStatistics.get(post)[2] = post.getNumberOfComments();
            previousStatistics.get(post)[3]++;

        }

        return reward;

    }

    public boolean isClosed() { return closed; }

    @Override
    public void close() throws IOException {

        if (!isClosed()) {
            multicastSocket.leaveGroup(group, networkInterface);
            multicastSocket.close();
        }

        closed = true;

    }

}