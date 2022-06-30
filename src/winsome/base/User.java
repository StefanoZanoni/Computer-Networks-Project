package winsome.base;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class User {

    private final AtomicInteger ID = new AtomicInteger(0);
    private final String username;
    private final char[] password;
    private final List<String> tags;
    private final Wallet wallet = new Wallet();

    public User(String username, char[] password, List<String> tags) {

        if (username == null || password == null || tags == null)
            throw new NullPointerException();

        ID.getAndIncrement();
        this.username = username;
        this.password = password.clone();
        this.tags = tags;

    }

    public String getUsername() { return username; }
    public char[] getPassword() { return password; }
    public List<String> getTags() { return tags; }
    public int getID() { return ID.get(); }

}