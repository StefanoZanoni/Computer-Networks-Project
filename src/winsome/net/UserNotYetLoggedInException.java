package winsome.net;

public class UserNotYetLoggedInException extends Exception {

    public UserNotYetLoggedInException() { super(); }
    UserNotYetLoggedInException(String message) { super(message); }

}
