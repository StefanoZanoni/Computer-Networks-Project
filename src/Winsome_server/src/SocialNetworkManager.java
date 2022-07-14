import winsome.base.Post;
import winsome.base.User;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SocialNetworkManager {

    private final ConcurrentHashMap<Integer, List<User>> network = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, List<Post>> blogs = new ConcurrentHashMap<>();


}
