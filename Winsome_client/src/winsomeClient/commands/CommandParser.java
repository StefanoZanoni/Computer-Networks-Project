package winsomeClient.commands;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandParser implements AutoCloseable {

    private boolean closed = false;
    private String command;
    private final LinkedList<String> arguments = new LinkedList<>();
    private final Scanner scanner = new Scanner(System.in);

    public void parse() throws UnknownCommandException {

        List<String> words = new ArrayList<>();
        Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
        Matcher regexMatcher = regex.matcher(scanner.nextLine());
        while (regexMatcher.find()) {
            if (regexMatcher.group(1) != null) {
                // add double-quoted string without the quotes
                words.add(regexMatcher.group(1));
            } else if (regexMatcher.group(2) != null) {
                // add single-quoted string without the quotes
                words.add(regexMatcher.group(2));
            } else {
                // add unquoted word
                words.add(regexMatcher.group());
            }
        }

        command = words.get(0);
        if ( command.compareTo("list") == 0 || command.compareTo("show") == 0 ||
                (command.compareTo("wallet") == 0 && !words.subList(1, words.size()).isEmpty()) )
            command = command.concat(" " + words.remove(1));

        switch (command) {

            case "register" -> {
                if (words.size() < 3 || words.size() > 8)
                    throw new IllegalArgumentException("The number of inserted arguments is not valid\n");
            }

            case "post" -> {
                if (words.size() != 3)
                    throw new IllegalArgumentException("The number of inserted arguments is not valid\n");
                if (words.get(1).length() > 20)
                    throw new IllegalArgumentException("Title length is greater than 20 characters\n");
                if (words.get(2).length() > 500)
                    throw new IllegalArgumentException("Content length is greater than 500 characters\n");
            }

            case "login", "comment", "rate" -> {
                if (words.size() != 3)
                    throw new IllegalArgumentException("The number of inserted arguments is not valid\n");
            }

            case "logout", "blog", "wallet", "list users", "list following",
                                "list followers", "show feed", "wallet btc" -> {
                if (words.size() > 1)
                    throw new IllegalArgumentException("The number of inserted arguments is not valid\n");
            }

            case "follow", "rewin", "delete", "unfollow", "show post" -> {
                if (words.size() != 2)
                    throw new IllegalArgumentException("The number of inserted arguments is not valid\n");
            }

            default -> throw new UnknownCommandException();

        }

        arguments.addAll(words.subList(1, words.size()));

    }

    public String getCommand() { return command; }
    public List<String> getArguments() {
        List<String> temp = new LinkedList<>(arguments);
        arguments.clear();
        return temp;
    }

    public boolean isClosed() { return closed; }
    @Override
    public void close() {

        if (!isClosed())
            scanner.close();

        closed = true;

    }

}