package base;

import java.nio.CharBuffer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class User {

    private final String username;
    private final char[] password;
    private final List<String> tags;
    private final HashMap<Integer, Post> blog = new LinkedHashMap<>();
    private final HashMap<Integer, Post> feed = new LinkedHashMap<>(20);
    private final Wallet wallet = new Wallet();

    public User(String username, char[] password, List<String> tags) {

        if (username == null || password == null || tags == null)
            throw new NullPointerException();

        this.username = username;
        this.password = password.clone();
        this.tags = tags;

    }

    public boolean validatePassword(char[] password) {

        String regex = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{8,20}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(CharBuffer.wrap(password));
        return matcher.matches();

    }

    public String getUsername() { return username; }
    public char[] getPassword() { return password; }
    public List<String> getTags() { return tags; }

    public void createPost(String title, String content) {

        Post post = new Post(this.username, title, content);
        blog.put(post.getID(), post);

    }
    public ArrayList<Post> getBlog() {

        Collection<Post> myPosts = blog.values();
        return new ArrayList<>(myPosts);

    }

}