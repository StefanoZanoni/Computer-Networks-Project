package commands;

public class UnknownCommandException extends Exception {

    public UnknownCommandException() { super(); }

    public UnknownCommandException(String e) { super(e); }

}
