import base.Post;
import base.User;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkManager {

    private final ConcurrentHashMap<User, List<User>> network = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<User, List<Post>> blogs = new ConcurrentHashMap<>();


}
