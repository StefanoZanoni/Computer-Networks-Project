package winsomeClient.tcp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import winsomeClient.ClientMain;
import winsomeClient.commands.UnknownCommandException;
import winsome.base.Post;
import winsome.base.Wallet;
import winsome.net.NetError;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.*;
import java.nio.ByteBuffer;
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

    public void establishConnection(InetAddress host, int port) {

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
                        register(arguments.get(0), arguments.get(1),
                                arguments.subList(2, arguments.size()));
                        receiveReferences();
                    }

                    case "login" -> {
                        send(command, arguments.get(0), arguments.get(1));
                        receiveReferences();
                    }

                    case "post" -> {
                        send(command, arguments.get(0), arguments.get(1));
                        receive();
                    }

                    case "logout" -> {
                        send(command);
                        receive();
                    }

                    case "list users", "list following" -> {
                        send(command);
                        receiveUsers();
                    }

                    case "blog", "show feed" -> {
                        send(command);
                        receivePosts();
                    }

                    case "wallet", "wallet btc" -> {
                        send(command);
                        receiveWallet();
                    }

                    case "delete", "rewin" -> {
                        send(command, Integer.parseInt(arguments.get(0)));
                        receive();
                    }

                    case "follow", "unfollow" -> {
                        send(command, arguments.get(0));
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

    @SafeVarargs
    private <T> void send(String command, T... arguments) {

        command = command.replaceAll("\\s+", "");
        command = command.toUpperCase();

        String request = command;
        Iterator<T> iterator = Arrays.stream(arguments).iterator();
        while (iterator.hasNext()) {
            request = request.concat("|");
            T argument = iterator.next();
            request = request.concat(argument.toString());
        }
        int bufferCapacity = request.getBytes(StandardCharsets.UTF_8).length;

        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(bufferCapacity);
        try {
            buffer.flip();
            socketChannel.write(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        buffer = ByteBuffer.allocate(bufferCapacity);
        buffer.put( request.getBytes(StandardCharsets.UTF_8) );

        try {
            buffer.flip();
            socketChannel.write(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void receiveReferences() {

        int bufferCapacity = Integer.BYTES;
        ByteBuffer buffer = ByteBuffer.allocate(bufferCapacity);
        try {
            socketChannel.read(buffer);
            buffer.flip();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        bufferCapacity = buffer.getInt();

        if (bufferCapacity == Integer.BYTES) {

            try {
                buffer.clear();
                socketChannel.read(buffer);
                buffer.flip();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            NetError error = NetError.valueOf(buffer.getInt());
            error.showError();
            ClientMain.correctIdentification = false;
            ClientMain.error = true;

        }
        else {

            buffer = ByteBuffer.allocate(bufferCapacity);
            try {
                socketChannel.read(buffer);
                buffer.flip();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String outcome = StandardCharsets.UTF_8.decode(buffer).toString();
            String[] references = outcome.split("\\|");
            Gson gson = new Gson();
            Type inetAddress = new TypeToken<InetAddress>() {}.getType();
            ClientMain.multicastIP = gson.fromJson(references[0], inetAddress);
            ClientMain.multicastPort = Integer.parseInt(references[1]);
            ClientMain.rmiCallbackName = references[2];
            ClientMain.rmiCallbackPort = Integer.parseInt(references[3]);
            ClientMain.correctIdentification = true;
            ClientMain.error = false;

        }

    }

    public void receive() {

        int bufferCapacity = Integer.BYTES;
        ByteBuffer buffer = ByteBuffer.allocate(bufferCapacity);
        try {
            socketChannel.read(buffer);
            buffer.flip();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int outcome = buffer.getInt();
        if (outcome >= 1000) {
            System.out.println("< Post created successfully: ID " + outcome);
            ClientMain.error = false;
        }
        else {
            NetError error = NetError.valueOf(outcome);
            error.showError();
            ClientMain.error = true;
        }

    }

    public void receiveUsers() {

        int bufferCapacity = Integer.BYTES;
        ByteBuffer buffer = ByteBuffer.allocate(bufferCapacity);
        try {
            socketChannel.read(buffer);
            buffer.flip();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        bufferCapacity = buffer.getInt();
        if (bufferCapacity == Integer.BYTES) {

            try {
                buffer.clear();
                socketChannel.read(buffer);
                buffer.flip();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            NetError error = NetError.valueOf(buffer.getInt());
            error.showError();
            ClientMain.error = true;

        }
        else if (bufferCapacity == 0) {
            System.out.println("< No users match the request");
            ClientMain.error = true;
        }
        else {

            buffer = ByteBuffer.allocate(bufferCapacity);
            try {
                socketChannel.read(buffer);
                buffer.flip();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String outcome = StandardCharsets.UTF_8.decode(buffer).toString();
            String[] data = outcome.split("\\|");
            Gson gson = new Gson();
            Type listOfString = new TypeToken<List<String>>() {}.getType();

            System.out.printf("< %s %-1c %s\n", "User", ':', "Tags");
            System.out.println("< ---------------------------");
            for (int i = 0; i < data.length; i = i + 2) {
                String username = data[i];
                List<String> tags = gson.fromJson(data[i + 1], listOfString);
                System.out.println("< " + username + ": " + tags);
            }
            ClientMain.error = false;

        }

    }

    public void receivePosts() {

        int bufferCapacity = Integer.BYTES;
        ByteBuffer buffer = ByteBuffer.allocate(bufferCapacity);
        try {
            socketChannel.read(buffer);
            buffer.flip();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        bufferCapacity = buffer.getInt();
        if (bufferCapacity == Integer.BYTES) {

            try {
                buffer.clear();
                socketChannel.read(buffer);
                buffer.flip();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            NetError error = NetError.valueOf(buffer.getInt());
            error.showError();
            ClientMain.error = true;

        }
        else if (bufferCapacity == 0) {
            System.out.println("< No posts have been published yet");
            ClientMain.error = true;
        }
        else {

            buffer = ByteBuffer.allocate(bufferCapacity);
            try {
                socketChannel.read(buffer);
                buffer.flip();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String outcome = StandardCharsets.UTF_8.decode(buffer).toString();
            String[] data = outcome.split("\\|");

            System.out.printf("< %s | %s | %s\n", "ID", "Author", "Title");
            System.out.println("< -------------------------------------------");
            for (int i = 0; i < data.length; i = i + 3) {
                int id = Integer.parseInt(data[i]);
                String owner = data[i+1];
                String title = data[i+2];
                System.out.println("< " + id + " | " + owner + " | " + title);
            }
            ClientMain.error = false;

        }

    }

    public void receivePost() {

        int bufferCapacity = Integer.BYTES;
        ByteBuffer buffer = ByteBuffer.allocate(bufferCapacity);
        try {
            socketChannel.read(buffer);
            buffer.flip();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        bufferCapacity = buffer.getInt();
        if (bufferCapacity == Integer.BYTES) {

            try {
                buffer.clear();
                socketChannel.read(buffer);
                buffer.flip();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            NetError error = NetError.valueOf(buffer.getInt());
            error.showError();
            ClientMain.error = true;

        }
        else {

            buffer = ByteBuffer.allocate(bufferCapacity);
            try {
                socketChannel.read(buffer);
                buffer.flip();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String outcome = StandardCharsets.UTF_8.decode(buffer).toString();
            Gson gson = new Gson();
            Post post = gson.fromJson(outcome, Post.class);
            System.out.println(post);
            ClientMain.error = false;

        }

    }

    public void receiveWallet() {

        int bufferCapacity = Integer.BYTES;
        ByteBuffer buffer = ByteBuffer.allocate(bufferCapacity);
        try {
            socketChannel.read(buffer);
            buffer.flip();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        bufferCapacity = buffer.getInt();
        if (bufferCapacity == Integer.BYTES) {

            try {
                buffer.clear();
                socketChannel.read(buffer);
                buffer.flip();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            NetError error = NetError.valueOf(buffer.getInt());
            error.showError();
            ClientMain.error = true;

        }
        else {

            buffer = ByteBuffer.allocate(bufferCapacity);
            try {
                socketChannel.read(buffer);
                buffer.flip();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String outcome = StandardCharsets.UTF_8.decode(buffer).toString();
            Gson gson = new Gson();
            Wallet wallet = gson.fromJson(outcome, Wallet.class);
            System.out.println(wallet);
            ClientMain.error = false;

        }

    }

    private void register(String username, String password, List<String> tags) {

        if (username == null || password == null || tags == null)
            throw new NullPointerException();

        while (!validateUsername(username)) {

            Scanner scanner = new Scanner(System.in);
            System.out.println("\ninvalid username, please enter a new username respecting the following format\n\n");
            System.out.println("1) username must contains only alphanumeric characters, underscore and dot\n");
            System.out.println("2) underscore and dot can't be at the end or start of the username\n");
            System.out.println("3) underscore and dot can't be next to each other\n");
            System.out.println("4) underscore or dot can't be used multiple times in a row\n");
            System.out.println("5) number of characters must be between 8 to 20\n");
            System.out.print("> ");
            username = scanner.next();

        }

        while (!validatePassword(password)) {

            Scanner scanner = new Scanner(System.in);
            System.out.println("\ninvalid password, please enter a new password respecting the following format\n\n");
            System.out.println("1) password must contains at least 8 characters\n");
            System.out.println("2) contains at least one digit\n");
            System.out.println("3) contains at least one lower case letter and one upper case letter\n");
            System.out.println("4) a special character must occur at least once\n");
            System.out.println("5) no whitespace allowed in the entire password\n");
            System.out.print("> ");
            password = scanner.next();

        }

        if (tags.isEmpty())
            send("register", username, password);
        else if (tags.size() == 1)
            send("register", username, password, tags.get(0));
        else if (tags.size() == 2)
            send("register", username, password, tags.get(0), tags.get(1));
        else if (tags.size() == 3)
            send("register", username, password, tags.get(0), tags.get(1), tags.get(2));
        else if (tags.size() == 4)
            send("register", username, password, tags.get(0), tags.get(1), tags.get(2), tags.get(3));
        else if (tags.size() == 5)
            send("register", username, password, tags.get(0), tags.get(1), tags.get(2),
                                        tags.get(3), tags.get(4));

    }

    private boolean validateUsername(String username) {

        String regex = "^(?=.{8,20}$)(?![_.])(?!.*[_.]{2})[a-zA-Z\\d._]+(?<![_.])$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(username);
        return matcher.matches();

    }

    private boolean validatePassword(String password) {

        String regex = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–{}:;',?/*~$^+=<>]).{8,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(password);
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