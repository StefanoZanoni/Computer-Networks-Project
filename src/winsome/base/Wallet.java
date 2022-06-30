package winsome.base;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public final class Wallet {

    private static class Transaction {

        private final float increment;
        private final String timestamp;

        public Transaction(float increment) {

            this.increment = increment;
            timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());

        }

        public String getTimestamp() { return timestamp; }

        public float getIncrement() { return increment; }

    }

    private float rewards = 0;
    private final List<Transaction> transactionsHistory = new LinkedList<>();

    public void addTransaction(Transaction transaction) {

        transactionsHistory.add(transaction);
        rewards += transaction.getIncrement();

    }

    public List<Transaction> getTransactions() { return transactionsHistory; }

    public float getRewards() { return rewards; }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof Wallet wallet)) return false;
        return Float.compare(wallet.getRewards(), getRewards()) == 0
                && transactionsHistory.equals(wallet.transactionsHistory);

    }

    @Override
    public int hashCode() { return Objects.hash(getRewards()); }

}
