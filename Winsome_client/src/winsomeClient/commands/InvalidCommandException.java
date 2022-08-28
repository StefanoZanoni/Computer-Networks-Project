package winsomeClient.commands;

public class InvalidCommandException extends Exception {

    public InvalidCommandException() { super(); }
    public InvalidCommandException(String message) { super(message); }

}
