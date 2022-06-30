package commands;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class CommandParser {

    String command;
    LinkedList<String> arguments = new LinkedList<>();
    Scanner scanner = new Scanner(System.in);

    public void parse() {

        String[] words = scanner.next().split(" ");
        command = words[0];
        arguments.addAll(Arrays.asList(words).subList(1, words.length));
        if ( command.compareTo("list") == 0 || command.compareTo("show") == 0 ||
                (command.compareTo("wallet") == 0 && !arguments.isEmpty()) )
            command = command.concat(" " + arguments.removeFirst());

    }

    public String getCommand() { return command; }

    public List<String> getArguments() { return arguments; }

    public void close() { scanner.close(); }
}
