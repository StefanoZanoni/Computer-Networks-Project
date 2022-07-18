package winsome.net;

public class UserDoesNotExistException extends Exception {

    public UserDoesNotExistException() { super(); }
    UserDoesNotExistException(String message) { super(message); }

}
