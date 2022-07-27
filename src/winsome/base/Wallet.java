package winsome.base;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public final class Wallet {

    private static class Transaction {

        private final float increment;
        private final String timestamp;

        private Transaction(float increment) {

            this.increment = increment;
            timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());

        }

        public String getTimestamp() { return timestamp; }
        public float getIncrement() { return increment; }
        @Override
        public String toString() { return "gain: " + this.getIncrement() + " " + this.getTimestamp(); }

    }

    private float rewards = 0;
    private final List<Transaction> transactionsHistory = new LinkedList<>();

    public void addTransaction(float increment) {

        Transaction transaction  = new Transaction(increment);
        transactionsHistory.add(transaction);
        rewards += increment;

    }

    public List<Transaction> getTransactions() { return transactionsHistory; }
    public float getRewards() { return rewards; }
    public void setRewardsBTC(float rewardsBTC) { rewards = rewardsBTC; }
    @Override
    public String toString() {
        return "rewards: " + this.getRewards() + "\n" +
               "transactions: " + this.getTransactions();
    }

}