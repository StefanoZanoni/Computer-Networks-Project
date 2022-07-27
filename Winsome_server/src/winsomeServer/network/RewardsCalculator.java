package winsomeServer.network;

import winsome.base.Post;
import winsome.base.Wallet;

import java.util.*;

public class RewardsCalculator extends TimerTask {

    /*  ArrayList[0] : numberOfPreviousUpvotes
        ArrayList[1] : numberOfPreviousDownvotes
        ArrayList[2] : numberOfPreviousComments
        ArrayList[3] : age
    */
    HashMap<Post, int[]> previousStatistics = new HashMap<>();

    @Override
    public void run() {

        Collection<Post> posts = SocialNetworkManager.posts.values();

        for (Post post : posts) {

            int[] statistics = previousStatistics.putIfAbsent( post, new int[]{0, 0, 0, 0} );
            String author = post.getOwner();
            float reward;
            if (statistics != null)
                reward = post.computeReward(statistics[0], statistics[1], statistics[2], statistics[3]);
            else
                reward = post.computeReward(0, 0, 0, 0);

            SocialNetworkManager.users.get(author).getWallet().addTransaction(reward);

            previousStatistics.get(post)[0] = post.getUpvotes().size();
            previousStatistics.get(post)[1] = post.getDownvotes().size();
            previousStatistics.get(post)[2] = post.getComments().size();
            previousStatistics.get(post)[3]++;

        }

    }

}