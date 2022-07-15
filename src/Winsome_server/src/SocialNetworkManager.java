import winsome.base.Post;
import winsome.base.User;
import winsome.net.UserAlreadyLoggedInException;
import winsome.net.UsernameAlreadyExistsException;
import winsome.net.UsernameDoesNotExistException;
import winsome.net.WrongPasswordException;

import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SocialNetworkManager {

    // client -> user
    static ConcurrentHashMap<SocketChannel, String> connections = new ConcurrentHashMap<>();

    // username -> user: this is the list of all users in Winsome
    static ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();

    // tag -> list users: for each tag this hashmap stores the list of users having that tag
    static ConcurrentHashMap<String, List<User>> tagsNetwork = new ConcurrentHashMap<>(16);

    // user -> followed
    static ConcurrentHashMap<String, List<User>> usersNetwork = new ConcurrentHashMap<>();

    // user -> posts
    static ConcurrentHashMap<String, List<Post>> posts = new ConcurrentHashMap<>();

    public static void couple(SocketChannel client, String username) { connections.put(client, username); }
    public static void uncouple(SocketChannel client, String username) { connections.remove(client, username); }

    //complete with json
    public static void addUser(SocketChannel client, String username, char[] password, List<String> tags) throws UsernameAlreadyExistsException {

        if ( users.containsKey(username) )
            throw new UsernameAlreadyExistsException();

        User user = new User(username, password, tags);
        users.put(username, user);
        connections.put(client, username);

    }

    public static void checkUser(SocketChannel client, String username, char[] password)
            throws UsernameDoesNotExistException, UserAlreadyLoggedInException, WrongPasswordException {

        if (!users.containsKey(username))
            throw new UsernameDoesNotExistException();
        if (connections.containsValue(username))
            throw new UserAlreadyLoggedInException();
        if ( !Arrays.equals(users.get(username).getPassword(), password))
            throw new WrongPasswordException();

        connections.put(client, username);

    }


}
