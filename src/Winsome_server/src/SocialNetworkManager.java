import winsome.base.Post;
import winsome.base.User;
import winsome.net.*;

import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SocialNetworkManager {

    // client -> user
    static ConcurrentHashMap<SocketChannel, String> connections = new ConcurrentHashMap<>();

    // username -> user: this is the list of all users in Winsome
    static ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();

    // tag -> list users: for each tag this hashmap stores the list of users having that tag
    static ConcurrentHashMap<String, List<User>> tagsNetwork = new ConcurrentHashMap<>();

    // user -> followed
    static ConcurrentHashMap<String, List<User>> usersNetwork = new ConcurrentHashMap<>();

    // user -> posts
    static ConcurrentHashMap<String, List<Post>> postsNetwork = new ConcurrentHashMap<>();

    // id -> post
    static ConcurrentHashMap<Integer, Post> posts = new ConcurrentHashMap<>();

    public static void couple(SocketChannel client, String username) { connections.put(client, username); }
    public static void uncouple(SocketChannel client, String username) { connections.remove(client, username); }

    //complete with json
    public static void addUser(SocketChannel client, String username, char[] password, List<String> tags) throws UsernameAlreadyExistsException {

        if ( users.containsKey(username) )
            throw new UsernameAlreadyExistsException();

        User user = new User(username, password, tags);
        Arrays.fill(password, (char) 0);
        users.put(username, user);
        connections.put(client, username);
        usersNetwork.put(username, new LinkedList<>());
        postsNetwork.put(username, new LinkedList<>());
        for (String tag : tags) {
            if (tagsNetwork.containsKey(tag))
                tagsNetwork.get(tag).add(user);
            else {
                tagsNetwork.put(tag, new LinkedList<>());
                tagsNetwork.get(tag).add(user);
            }
        }

    }

    public static void checkUser(SocketChannel client, String username, char[] password)
            throws UserDoesNotExistException, UserAlreadyLoggedInException, WrongPasswordException {

        if ( !Arrays.equals(users.get(username).getPassword(), password) )
            throw new WrongPasswordException();
        Arrays.fill(password, (char) 0);

        if (!users.containsKey(username))
            throw new UserDoesNotExistException();
        if (connections.containsValue(username))
            throw new UserAlreadyLoggedInException();

        connections.put(client, username);

    }

    public static List<User> getLikeMindedUsers(SocketChannel client) throws UserNotYetLoggedInException {

        String username = connections.get(client);
        if (username == null)
            throw new UserNotYetLoggedInException();

        User user = users.get(username);
        List<String> userTags = user.getTags();
        List<User> likeMindedUsers = new LinkedList<>();

        for (String tag : userTags) {
            List<User> temp = tagsNetwork.get(tag);
            int indexOfCurrentUser = temp.indexOf(user);
            List<User> temp1 = temp.subList(0, indexOfCurrentUser);
            List<User> temp2 = temp.subList(indexOfCurrentUser + 1, temp.size());
            if (temp1.isEmpty() && temp2.isEmpty())
                continue;
            likeMindedUsers.addAll(temp1);
            likeMindedUsers.addAll(temp2);
        }

        if (likeMindedUsers.isEmpty())
            return Collections.emptyList();

        return likeMindedUsers;

    }

    public static List<User> getFollowed(SocketChannel client) throws UserNotYetLoggedInException {

        String username = connections.get(client);
        if (username == null)
            throw new UserNotYetLoggedInException();

        List<User> followed = usersNetwork.get(username);
        if (followed.isEmpty())
            return Collections.emptyList();

        return followed;

    }

    public static void addFollowing(SocketChannel client, String username)
            throws UserNotYetLoggedInException, UserDoesNotExistException {

        String user = connections.get(client);
        if (user == null)
            throw new UserNotYetLoggedInException();

        if (!user.equals(username)) {
            User newFollowed = users.get(username);
            if (newFollowed == null)
                throw new UserDoesNotExistException();
            List<User> followed = usersNetwork.get(user);
            if (!followed.contains(newFollowed))
                followed.add(newFollowed);
        }

    }

    public static void removeFollowing(SocketChannel client, String username)
            throws UserNotYetLoggedInException, UserDoesNotExistException {

        String user = connections.get(client);
        if (username == null)
            throw new UserNotYetLoggedInException();

        User oldFollowed = users.get(username);
        if (oldFollowed == null)
            throw new UserDoesNotExistException();

        usersNetwork.get(user).remove(oldFollowed);

    }

    public static List<Post> getPosts(SocketChannel client) throws UserNotYetLoggedInException {

        String username = connections.get(client);
        if (username == null)
            throw new UserNotYetLoggedInException();

        List<Post> posts1 = postsNetwork.get(username);

        if (posts1.isEmpty())
            return Collections.emptyList();

        return posts1;

    }

    public static void addPost(SocketChannel client, String title, String content) throws UserNotYetLoggedInException {

        String username = connections.get(client);
        if (username == null)
            throw new UserNotYetLoggedInException();

        Post newPost = new Post(username, title, content);
        posts.put(newPost.getID(), newPost);
        postsNetwork.get(username).add(newPost);

    }

    public static List<Post> getFeed(SocketChannel client) throws UserNotYetLoggedInException {

        String username = connections.get(client);
        if (username == null)
            throw new UserNotYetLoggedInException();

        List<User> followed = usersNetwork.get(username);

        if (followed.isEmpty())
            return Collections.emptyList();
        List<Post> posts1 = new LinkedList<>();
        for (User user : followed) {
            posts1.addAll( postsNetwork.get(user.getUsername()) );
        }
        if (posts1.isEmpty())
            return Collections.emptyList();

        return posts1;

    }

    public static Post getPost(int idPost) throws PostDoesNotExistException {

        Post post = posts.get(idPost);
        if (post == null)
            throw new PostDoesNotExistException();

        return post;
    }

    public static void deletePost(SocketChannel client, int idPost)
            throws UserNotYetLoggedInException, PostDoesNotExistException, UserIsNotTheOwnerException {

        String username = connections.get(client);
        if (username == null)
            throw new UserNotYetLoggedInException();

        Post post = posts.get(idPost);
        if (post == null)
            throw new PostDoesNotExistException();

        if (!post.getOwner().equals(username))
            throw new UserIsNotTheOwnerException();

        posts.remove(idPost, post);
        postsNetwork.get(username).remove(post);

    }


}
