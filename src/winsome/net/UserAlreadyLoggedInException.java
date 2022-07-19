package winsome.net;

public class UserAlreadyLoggedInException extends Exception {

    public UserAlreadyLoggedInException() { super(); }
    public UserAlreadyLoggedInException(String message) { super(message); }

}
