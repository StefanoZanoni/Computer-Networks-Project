package winsomeServer.network;

import winsome.base.Post;
import winsome.base.User;
import winsome.base.Wallet;
import winsome.net.exceptions.*;
import winsomeServer.rmi.ServerRMIManager;

import java.io.IOException;
import java.net.URL;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SocialNetworkManager {

    // client -> user
    public static final ConcurrentHashMap<SocketChannel, String> connections = new ConcurrentHashMap<>();

    // username -> user: this is the list of all users in Winsome
    public static ConcurrentHashMap<String, User> users;

    // tag -> list users: for each tag this hashmap stores the list of users having that tag
    public static ConcurrentHashMap<String, List<String>> tagsNetwork;

    // user -> followed
    public static ConcurrentHashMap<String, List<String>> usersNetwork;

    // user -> posts
    public static ConcurrentHashMap<String, List<Integer>> postsNetwork;

    // id -> post
    public static ConcurrentHashMap<Integer, Post> posts;

    public static ServerRMIManager rmiManager;

    public static void uncouple(SocketChannel client) throws UserNotYetLoggedInException {

        if (!connections.containsKey(client))
            throw new UserNotYetLoggedInException();

        connections.remove(client);

    }

    //complete with json
    public static void addUser(SocketChannel client, String username, char[] password, List<String> tags)
            throws UsernameAlreadyExistsException {

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
                tagsNetwork.get(tag).add(username);
            else {
                tagsNetwork.put(tag, new LinkedList<>());
                tagsNetwork.get(tag).add(username);
            }
        }

    }

    public static void checkUser(SocketChannel client, String username, char[] password)
            throws UserDoesNotExistException, UserAlreadyLoggedInException, WrongPasswordException {

        if (connections.containsValue(username))
            throw new UserAlreadyLoggedInException();
        if (!users.containsKey(username))
            throw new UserDoesNotExistException();
        if ( !Arrays.equals(users.get(username).getPassword(), password) )
            throw new WrongPasswordException();
        Arrays.fill(password, (char) 0);

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
            List<String> usernames = tagsNetwork.get(tag);
            List<User> temp = new LinkedList<>();
            for (String tempUsername : usernames) {
                User tempUser = users.get(tempUsername);
                temp.add(tempUser);
            }
            int indexOfCurrentUser = temp.indexOf(user);
            if (indexOfCurrentUser != -1) {
                List<User> temp1 = temp.subList(0, indexOfCurrentUser);
                List<User> temp2 = temp.subList(indexOfCurrentUser, temp.size());
                if (!temp1.isEmpty())
                    for (User user1 : temp1) {
                        if (!user.equals(user1))
                            if (!likeMindedUsers.contains(user1))
                                likeMindedUsers.add(user1);
                    }
                if (!temp2.isEmpty())
                    for (User user1 : temp2) {
                        if (!user.equals(user1))
                            if (!likeMindedUsers.contains(user1))
                                likeMindedUsers.add(user1);
                    }
            }
        }

        if (likeMindedUsers.isEmpty())
            return Collections.emptyList();

        return likeMindedUsers;

    }

    public static List<User> getFollowed(SocketChannel client) throws UserNotYetLoggedInException {

        String username = connections.get(client);
        if (username == null)
            throw new UserNotYetLoggedInException();

        List<String> usernames = usersNetwork.get(username);
        List<User> followed = new LinkedList<>();
        for (String tempUsername : usernames) {
            User user = users.get(tempUsername);
            followed.add(user);
        }
        if (followed.isEmpty())
            return Collections.emptyList();

        return followed;

    }

    public static void addFollowing(SocketChannel client, String username)
            throws UserNotYetLoggedInException, UserDoesNotExistException, SelfFollowingException {

        String user = connections.get(client);
        if (user == null)
            throw new UserNotYetLoggedInException();
        if (user.equals(username))
            throw new SelfFollowingException();
        if (!users.containsKey(username))
            throw new UserDoesNotExistException();

        List<String> followed = usersNetwork.get(user);
        if (!followed.contains(username))
            followed.add(username);

        rmiManager.update(username, user, true);

    }

    public static void removeFollowing(SocketChannel client, String username)
            throws UserNotYetLoggedInException, UserDoesNotExistException, UserNotFollowedException {

        String user = connections.get(client);

        if (user == null)
            throw new UserNotYetLoggedInException();
        if (!users.containsKey(username))
            throw new UserDoesNotExistException();
        if (!usersNetwork.get(user).contains(username))
            throw new UserNotFollowedException();

        usersNetwork.get(user).remove(username);

        rmiManager.update(username, user, false);

    }

    public static List<Post> getPosts(SocketChannel client) throws UserNotYetLoggedInException {

        String username = connections.get(client);
        if (username == null)
            throw new UserNotYetLoggedInException();

        List<Integer> ids = postsNetwork.get(username);
        List<Post> tempPosts = new LinkedList<>();
        for (Integer id : ids) {
            Post post = posts.get(id);
            tempPosts.add(post);
        }

        if (tempPosts.isEmpty())
            return Collections.emptyList();

        return tempPosts;

    }

    public static int addPost(SocketChannel client, String title, String content) throws UserNotYetLoggedInException {

        String username = connections.get(client);
        if (username == null)
            throw new UserNotYetLoggedInException();

        Post newPost = new Post(username, title, content, posts.keySet().size());
        int ID = newPost.getID();
        posts.put(ID, newPost);
        postsNetwork.get(username).add(ID);

        return ID;

    }

    public static List<Post> getFeed(SocketChannel client) throws UserNotYetLoggedInException {

        String username = connections.get(client);
        if (username == null)
            throw new UserNotYetLoggedInException();

        List<String> followed = usersNetwork.get(username);

        if (followed.isEmpty())
            return Collections.emptyList();
        List<Post> tempPosts = new LinkedList<>();
        for (String tempUsername : followed) {
            List<Integer> ids = postsNetwork.get(tempUsername);
            List<Post> userPosts = new LinkedList<>();
            for (Integer id : ids) {
                Post post = posts.get(id);
                userPosts.add(post);
            }
            for (Post post : userPosts) {
                if (!tempPosts.contains(post))
                    tempPosts.add(post);
            }
        }
        if (tempPosts.isEmpty())
            return Collections.emptyList();

        return tempPosts;

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

        postsNetwork.get(username).remove((Integer) idPost);
        posts.remove(idPost, post);

    }

    public static void rewin(SocketChannel client, int idPost)
            throws UserNotYetLoggedInException, PostNotInTheFeedException, PostDoesNotExistException {

        List<Post> feed = SocialNetworkManager.getFeed(client);
        String username = connections.get(client);
        if (!posts.containsKey(idPost))
            throw new PostDoesNotExistException();
        if (!feed.contains(posts.get(idPost)))
            throw new PostNotInTheFeedException();

        postsNetwork.get(username).add(idPost);

    }

    public static void ratePost(SocketChannel client, int idPost, int vote)
            throws UserNotYetLoggedInException, PostDoesNotExistException, PostNotInTheFeedException,
            PostAlreadyVotedException, UserIsTheAuthorException, InvalidVoteException {

        List<Post> feed = SocialNetworkManager.getFeed(client);
        String username = connections.get(client);
        Post post = posts.get(idPost);
        if (post == null)
            throw new PostDoesNotExistException();
        if (post.getOwner().equals(username))
            throw new UserIsTheAuthorException();
        if (!feed.contains(post))
            throw new PostNotInTheFeedException();
        if (post.getUpvotes().contains(username))
            throw new PostAlreadyVotedException();
        if (post.getDownvotes().contains(username))
            throw new PostAlreadyVotedException();
        if (vote != 1 && vote != -1)
            throw new InvalidVoteException();

        if (vote == 1)
            post.addUpvote(username);
        else
            post.addDownvote(username);

    }

    public static void addComment(SocketChannel client, int idPost, String text)
            throws UserNotYetLoggedInException, UserIsTheAuthorException, PostNotInTheFeedException {

        List<Post> feed = SocialNetworkManager.getFeed(client);
        Post post = posts.get(idPost);
        if (!feed.contains(post))
            throw new PostNotInTheFeedException();
        String username = connections.get(client);
        if (post.getOwner().compareTo(username) == 0)
            throw new UserIsTheAuthorException();

        post.addComment(username, text);

    }

    public static Wallet getWallet(SocketChannel client) throws UserNotYetLoggedInException {

        String username = connections.get(client);
        if (username == null)
            throw new UserNotYetLoggedInException();
        User user = users.get(username);

        return user.getWallet();

    }

    public static Map<Float, Wallet> getWalletBTC(SocketChannel client) throws UserNotYetLoggedInException, IOException {

        Map<Float, Wallet> returnPair = new HashMap<>(1);

        Wallet wallet = SocialNetworkManager.getWallet(client);

        URL url = new URL("https://www.random.org/decimal-fractions/?num=1&dec=10&col=1&format=plain&rnd=new");
        Scanner scanner = new Scanner(url.openStream());

        // exchangeRate is in [0.5, 1.5]
        float exchangeRate = Float.parseFloat(scanner.next()) + 1;
        returnPair.put(exchangeRate, wallet);
        float rewardsBTC = wallet.getRewards() * exchangeRate;
        wallet.setRewardsBTC(rewardsBTC);

        return returnPair;

    }

}