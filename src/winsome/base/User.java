package winsome.base;

import java.util.*;

public final class User {

    private final String username;
    private final char[] password;
    private final List<String> tags;
    private final Wallet wallet = new Wallet();

    public User(String username, char[] password, List<String> tags) {

        if (username == null || password == null || tags == null)
            throw new NullPointerException();

        this.username = username;
        this.password = password.clone();
        this.tags = tags;

    }

    public String getUsername() { return username; }
    public char[] getPassword() { return password; }
    public List<String> getTags() { return tags; }
    public Wallet getWallet() { return wallet; }


    @Override
    public String toString() {
        return "user: " + username + "\ntags: " + tags.toString() + "\nwallet: " + wallet;
    }

}