package winsome.net;

public class UserAlreadyLoggedInException extends Exception {

    public UserAlreadyLoggedInException() { super(); }
    UserAlreadyLoggedInException(String message) { super(message); }

}
