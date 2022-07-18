package winsome.net;

public class PostDoesNotExistException extends Exception {

    public PostDoesNotExistException() { super(); }
    PostDoesNotExistException(String message) { super(message); }

}
