package winsome.net;

public class WrongPasswordException extends Exception {

    public WrongPasswordException() { super(); }
    WrongPasswordException(String message) { super(message); }

}
