package winsome.net;

public class UsernameAlreadyExistsException extends Exception{

    public UsernameAlreadyExistsException() { super(); }
    UsernameAlreadyExistsException(String message) { super(message); }

}
