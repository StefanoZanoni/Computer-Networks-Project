package winsomeServer.network;

import winsome.base.Post;

import java.util.*;

public class RewardsCalculator extends TimerTask {

    /*
        int[0] : numberOfPreviousUpvotes
        int[1] : numberOfPreviousDownvotes
        int[2] : numberOfPreviousComments
        int[3] : age
    */
    HashMap<Post, int[]> previousStatistics = new HashMap<>();

    @Override
    public void run() {

        for (Post post : SocialNetworkManager.posts.values()) {

            String author = post.getOwner();
            float reward;
            int[] statistics = previousStatistics.putIfAbsent( post, new int[]{0, 0, 0, 1} );
            if (statistics != null)
                reward = post.computeReward(statistics[0], statistics[1], statistics[2], statistics[3]);
            else
                reward = post.computeReward(0, 0, 0, 1);

            if (reward != 0)
                SocialNetworkManager.users.get(author).getWallet().addTransaction(reward);

            previousStatistics.get(post)[0] = post.getUpvotes().size();
            previousStatistics.get(post)[1] = post.getDownvotes().size();
            previousStatistics.get(post)[2] = post.getComments().size();
            previousStatistics.get(post)[3]++;

        }

    }

}