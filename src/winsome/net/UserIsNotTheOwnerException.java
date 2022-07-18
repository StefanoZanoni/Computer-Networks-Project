package winsome.net;

public class UserIsNotTheOwnerException extends Exception {

    public UserIsNotTheOwnerException() { super(); }
    UserIsNotTheOwnerException(String message) { super(message); }

}
