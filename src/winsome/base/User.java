package winsome.base;

import java.util.*;

public final class User {

    private final String username;
    private final char[] password;
    private final List<String> tags;
    private final Wallet wallet;

    public User(String username, char[] password, List<String> tags) {

        if (username == null || password == null || tags == null)
            throw new NullPointerException();

        this.username = username;
        this.password = password.clone();
        this.tags = tags;
        wallet = new Wallet();

    }

    public String getUsername() { return username; }
    public char[] getPassword() { return password; }
    public List<String> getTags() { return tags; }
    public Wallet getWallet() { return wallet; }

    @Override
    public String toString() {
        return  "< user: " + this.getUsername() + "\n" +
                "< tags: " + this.getTags() + "\n" +
                "< wallet: " + this.getWallet();
    }

}