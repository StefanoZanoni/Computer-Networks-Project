package winsome.net.exceptions;

public class SelfFollowingException extends Exception {

    public SelfFollowingException() { super(); }
    public SelfFollowingException(String message) { super(message); }
}
