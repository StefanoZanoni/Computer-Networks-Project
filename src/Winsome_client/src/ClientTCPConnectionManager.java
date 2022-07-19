import commands.UnknownCommandException;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientTCPConnectionManager {
    SocketChannel socketChannel;

    public ClientTCPConnectionManager() {

        try {
            socketChannel = SocketChannel.open();
        } catch (IOException e) {
            e.printStackTrace(System.err);
            throw new RuntimeException("the client was unable to open the socket");
        }

    }

    public void establishConnection(String host, int port) {

        try {
            socketChannel.connect(new InetSocketAddress(host, port));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void interact(String command, List<String> arguments) throws UnknownCommandException {

         class CommandSelector {

            public void select(String command, List<String> arguments) throws UnknownCommandException {

                switch (command) {
                    case "register" -> register(arguments.get(0), arguments.get(1).toCharArray(),
                            arguments.subList(2, arguments.size()));

                    case "login", "post" -> send(command, arguments.get(0), arguments.get(1));

                    case "logout" -> send(command, arguments.get(0));

                    case "list users", "list followers", "list following",
                            "blog", "show feed", "wallet", "wallet btc" -> send(command);

                    case "follow", "unfollow", "show post",
                            "delete", "rewin" -> send(command, Integer.parseInt(arguments.get(0)));

                    case "rate" -> send(command, Integer.parseInt(arguments.get(0)), Integer.parseInt(arguments.get(1)));

                    case "comment" -> send(command, Integer.parseInt(arguments.get(0)), arguments.get(1));

                    default -> throw new UnknownCommandException(command + "is not a valid command");
                }

            }

        }

         CommandSelector commandSelector = new CommandSelector();
         commandSelector.select(command, arguments);

    }

    private void send(String command, Object... arguments) {

        int bufferCapacity;
        if (arguments.length == 0)
            bufferCapacity = command.length() + Character.BYTES;
        else {
            bufferCapacity = command.length() + Character.BYTES;
            int argumentsDim = (arguments.length - 1) * Character.BYTES;
            for (Object object : arguments) {
                if (object instanceof Integer)
                    argumentsDim += Integer.BYTES;
                else if (object instanceof String)
                    argumentsDim += ((String) object).length();
                else if (object instanceof char[])
                    argumentsDim += ((char[]) object).length;
            }
            bufferCapacity += argumentsDim;
        }

        if (bufferCapacity < Integer.BYTES)
            bufferCapacity = Integer.BYTES;

        ByteBuffer buffer = ByteBuffer.allocate(bufferCapacity);

        buffer.putInt(bufferCapacity);
        try {
            socketChannel.write(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        buffer.clear();

        command = command.replaceAll("\\s+", "");
        buffer.put( command.toUpperCase().getBytes(StandardCharsets.UTF_8) );
        buffer.put( "|".getBytes(StandardCharsets.UTF_8) );
        Iterator<Object> iterator = Arrays.stream(arguments).iterator();
        while (iterator.hasNext()) {
            Object object = iterator.next();
            if (object instanceof Integer) {
                buffer.putInt( (Integer) object );
                if (iterator.hasNext())
                    buffer.put( "|".getBytes(StandardCharsets.UTF_8) );
            }
            else if (object instanceof String) {
                buffer.put( ((String) object).getBytes(StandardCharsets.UTF_8) );
                if (iterator.hasNext())
                    buffer.put( "|".getBytes(StandardCharsets.UTF_8) );
            }
            else if (object instanceof char[]) {
                buffer.put( Arrays.toString(((char[]) object)).getBytes(StandardCharsets.UTF_8) );
                if (iterator.hasNext())
                    buffer.put( "|".getBytes(StandardCharsets.UTF_8) );
            }
        }

        try {
            socketChannel.write(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void receive() {



    }

    private void register(String username, char[] password, List<String> tags) {

        if (username == null || password == null || tags == null)
            throw new NullPointerException();

        if (tags.size() > 5) {
            System.out.println("have been inserted over 5 tags, only the first 5 will be maintain\n");
            tags.subList(5, tags.size()).clear();
        }

        while (!validateUsername(username)) {

            Scanner scanner = new Scanner(System.in);
            System.out.println("\n\ninvalid username, please enter a new username respecting the following format\n\n");
            System.out.println("1) username must contains only alphanumeric characters, underscore and dot\n");
            System.out.println("2) underscore and dot can't be at the end or start of the username\n");
            System.out.println("3) underscore and dot can't be next to each other\n");
            System.out.println("4) underscore or dot can't be used multiple times in a row\n");
            System.out.println("5) number of characters must be between 8 to 20\n");
            System.out.println("> ");
            username = scanner.next();

        }

        while (!validatePassword(password)) {

            Scanner scanner = new Scanner(System.in);
            System.out.println("\n\ninvalid password, please enter a new password respecting the following format\n\n");
            System.out.println("1) password must contains at least 8 characters\n");
            System.out.println("2) contains at least one digit\n");
            System.out.println("3) contains at least one lower case letter and one upper case letter\n");
            System.out.println("4) a special character must occur at least once\n");
            System.out.println("5) no whitespace allowed in the entire password\n");
            System.out.println("> ");
            password = scanner.next().toCharArray();

        }

        send("register", username, password, tags.get(0),
                tags.get(1), tags.get(2), tags.get(3), tags.get(4));

    }

    private boolean validateUsername(String username) {

        String regex = "^(?=.{8,20}$)(?![_.])(?!.*[_.]{2})[a-zA-Z\\d._]+(?<![_.])$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(username);
        return matcher.matches();

    }

    private boolean validatePassword(char[] password) {

        String regex = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(CharBuffer.wrap(password));
        return matcher.matches();

    }

    public void close() {

        try {
            socketChannel.close();
        } catch (IOException e) {
            throw new RuntimeException("error while closing the socket");
        }

    }
}
