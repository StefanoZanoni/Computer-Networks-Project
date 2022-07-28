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
    private final HashMap<Post, int[]> previousStatistics = new HashMap<>();
    private final float authorEarnPercentage;

    public RewardsCalculator(float authorEarnPercentage1) { this.authorEarnPercentage = authorEarnPercentage1; }

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

    }

}