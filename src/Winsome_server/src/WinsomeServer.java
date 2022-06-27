import base.Post;
import base.User;

import java.nio.CharBuffer;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WinsomeServer {

    private final ConcurrentHashMap<User, List<User>> network = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<User, List<Post>> blogs = new ConcurrentHashMap<>();

    public static void main() {



    }

    private boolean validatePassword(char[] password) {

        String regex = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{8,20}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(CharBuffer.wrap(password));
        return matcher.matches();

    }
}
