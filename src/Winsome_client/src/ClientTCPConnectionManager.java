import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import commands.UnknownCommandException;
import winsome.base.Post;
import winsome.base.Wallet;
import winsome.net.NetError;

import java.io.IOException;
import java.lang.reflect.Type;
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

    public void interact(String command, List<String> arguments) {

        class CommandSelector {

            public void select(String command, List<String> arguments) throws UnknownCommandException {

                switch (command) {

                    case "register" -> {
                        register(arguments.get(0), arguments.get(1).toCharArray(),
                                arguments.subList(2, arguments.size()));
                        receive();
                    }

                    case "login", "post" -> {
                        send(command, arguments.get(0), arguments.get(1));
                        receive();
                    }

                    case "logout" -> {
                        send(command, arguments.get(0));
                        receive();
                    }

                    case "list users", "list following" -> {
                        send(command);
                        receiveUsers();
                    }

                    case "list followers" -> {}

                    case "blog", "show feed" -> {
                        send(command);
                        receivePosts();
                    }

                    case "wallet", "wallet btc" -> {
                        send(command);
                        receiveWallet();
                    }

                    case "follow", "unfollow", "delete", "rewin" -> {
                        send(command, Integer.parseInt(arguments.get(0)));
                        receive();
                    }

                    case "show post" -> {
                        send(command, Integer.parseInt(arguments.get(0)));
                        receivePost();
                    }

                    case "rate" -> {
                        send(command, Integer.parseInt(arguments.get(0)), Integer.parseInt(arguments.get(1)));
                        receive();
                    }

                    case "comment" -> {
                        send(command, Integer.parseInt(arguments.get(0)), arguments.get(1));
                        receive();
                    }

                    default -> throw new UnknownCommandException(command + "is not a valid command");

                }

            }

        }

        CommandSelector commandSelector = new CommandSelector();
        try {
            commandSelector.select(command, arguments);
        } catch (UnknownCommandException ignored) {}

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

    public void receive() {

        int bufferCapacity = Integer.BYTES;
        ByteBuffer buffer = ByteBuffer.allocate(bufferCapacity);
        try {
            socketChannel.read(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        NetError error = NetError.valueOf(String.valueOf(buffer.getInt()));
        error.showError();

    }

    public void receiveUsers() {

        int bufferCapacity = Integer.BYTES;
        ByteBuffer buffer = ByteBuffer.allocate(bufferCapacity);
        try {
            socketChannel.read(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        bufferCapacity = buffer.getInt();
        if (bufferCapacity == Integer.BYTES) {

            buffer.clear();
            try {
                socketChannel.read(buffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            NetError error = NetError.valueOf(String.valueOf(buffer.getInt()));
            error.showError();

        }
        else {

            buffer = ByteBuffer.allocate(bufferCapacity);
            try {
                socketChannel.read(buffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String outcome = StandardCharsets.UTF_8.decode(buffer).toString();
            String[] data = outcome.split("\\|");
            Gson gson = new Gson();
            Type listOfString = new TypeToken<List<String>>() {}.getType();

            System.out.printf("%s %-1c %s\n", "User", ':', "Tags");
            System.out.println("---------------------------");
            for (int i = 0; i < data.length; i = i + 2) {
                String username = data[i];
                List<String> tags = gson.fromJson(data[i + 1], listOfString);
                System.out.println(username + ": " + tags);
            }

        }

    }

    public void receivePosts() {

        int bufferCapacity = Integer.BYTES;
        ByteBuffer buffer = ByteBuffer.allocate(bufferCapacity);
        try {
            socketChannel.read(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        bufferCapacity = buffer.getInt();
        if (bufferCapacity == Integer.BYTES) {

            buffer.clear();
            try {
                socketChannel.read(buffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            NetError error = NetError.valueOf(String.valueOf(buffer.getInt()));
            error.showError();

        }
        else {

            buffer = ByteBuffer.allocate(bufferCapacity);
            try {
                socketChannel.read(buffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String outcome = StandardCharsets.UTF_8.decode(buffer).toString();
            String[] data = outcome.split("\\|");

            System.out.printf("%s %s %s\n", "ID", "Author", "Title");
            System.out.println("-------------------------------------------");
            for (int i = 0; i < data.length; i = i + 3) {
                int id = Integer.parseInt(data[i]);
                String owner = data[i+1];
                String title = data[i+2];
                System.out.println(id + owner + title);
            }

        }

    }

    public void receivePost() {

        int bufferCapacity = Integer.BYTES;
        ByteBuffer buffer = ByteBuffer.allocate(bufferCapacity);
        try {
            socketChannel.read(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        bufferCapacity = buffer.getInt();
        if (bufferCapacity == Integer.BYTES) {

            buffer.clear();
            try {
                socketChannel.read(buffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            NetError error = NetError.valueOf(String.valueOf(buffer.getInt()));
            error.showError();

        }
        else {

            buffer = ByteBuffer.allocate(bufferCapacity);
            try {
                socketChannel.read(buffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String outcome = StandardCharsets.UTF_8.decode(buffer).toString();
            Gson gson = new Gson();
            Post post = gson.fromJson(outcome, Post.class);
            System.out.println(post);

        }

    }

    public void receiveWallet() {

        int bufferCapacity = Integer.BYTES;
        ByteBuffer buffer = ByteBuffer.allocate(bufferCapacity);
        try {
            socketChannel.read(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        bufferCapacity = buffer.getInt();
        if (bufferCapacity == Integer.BYTES) {

            buffer.clear();
            try {
                socketChannel.read(buffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            NetError error = NetError.valueOf(String.valueOf(buffer.getInt()));
            error.showError();

        }
        else {

            buffer = ByteBuffer.allocate(bufferCapacity);
            try {
                socketChannel.read(buffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String outcome = StandardCharsets.UTF_8.decode(buffer).toString();
            Gson gson = new Gson();
            Wallet wallet = gson.fromJson(outcome, Wallet.class);
            System.out.println(wallet);

        }

    }

    private void register(String username, char[] password, List<String> tags) {

        if (username == null || password == null || tags == null)
            throw new NullPointerException();

        if (tags.size() > 5) {
            System.out.println("have been inserted over 5 tags, only the first 5 will be maintained\n");
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