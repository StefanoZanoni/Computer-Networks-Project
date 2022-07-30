package winsomeServer.network;

import winsome.base.Post;
import winsomeServer.ServerMain;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.util.*;

public class RewardsCalculator extends TimerTask implements Closeable {

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

    public RewardsCalculator(float authorEarnPercentage) {

        this.authorEarnPercentage = authorEarnPercentage;

        byte[] data;
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(1);
        data = buffer.array();
        datagramPacket = new DatagramPacket(data, data.length,
                ServerMain.multicastIP, ServerMain.multicastPort);

        try {
            multicastSocket = new MulticastSocket(ServerMain.multicastPort);
            multicastSocket.setTimeToLive(1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public void run() {

        if (computeReward() != 0)
            try {
                multicastSocket.send(datagramPacket);
                System.out.println("sent");
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
    public void close() throws IOException { multicastSocket.close(); closed = true; }

}