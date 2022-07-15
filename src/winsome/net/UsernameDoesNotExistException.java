package winsome.net;

public class UsernameDoesNotExistException extends Exception {

    public UsernameDoesNotExistException() { super(); }
    UsernameDoesNotExistException(String message) { super(message); }

}
