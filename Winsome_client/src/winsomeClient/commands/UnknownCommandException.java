package winsomeClient.commands;

public class UnknownCommandException extends Exception {

    public UnknownCommandException() { super(); }
    public UnknownCommandException(String message) { super(message); }

}
