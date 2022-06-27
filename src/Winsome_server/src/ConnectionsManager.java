import java.io.IOException;
import java.net.ServerSocket;
import java.nio.CharBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnectionsManager {

    ServerSocket socket;

    ConnectionsManager() {
        try {
            socket = new ServerSocket(80);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean validatePassword(char[] password) {

        String regex = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{8,20}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(CharBuffer.wrap(password));
        return matcher.matches();

    }

}
