import commands.CommandParser;

import java.io.Closeable;
import java.io.IOException;
import java.net.*;
import java.nio.channels.SocketChannel;
import java.util.List;

public class TCPConnectionManager {
    SocketChannel socketChannel;

    public TCPConnectionManager() {

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

    public void sendCommand(String command, List<String> arguments) {

         class CommandSelector {

            public void select(String command, List<String> arguments) {

                switch (command) {
                    case "register" ->
                            register(arguments.get(0), arguments.get(1).toCharArray(), arguments.subList(2, arguments.size()));
                    case "login" -> login(arguments.get(0), arguments.get(1).toCharArray());
                    case "logout" -> logout(arguments.get(0));
                    case "list users" -> listUsers();
                    case "list followers" -> listFollowers();
                    case "list following" -> listFollowing();
                    case "follow" -> followUser(Integer.parseInt(arguments.get(0)));
                    case "unfollow" -> unfollowUser(Integer.parseInt(arguments.get(0)));
                    case "blog" -> viewBlog();
                    case "post" -> createPost(arguments.get(0), arguments.get(1));
                    case "show feed" -> showFeed();
                    case "show post" -> showPost(Integer.parseInt(arguments.get(0)));
                    case "delete" -> deletePost(Integer.parseInt(arguments.get(0)));
                    case "rewin" -> rewinPost(Integer.parseInt(arguments.get(0)));
                    case "rate" -> ratePost(Integer.parseInt(arguments.get(0)), Integer.parseInt(arguments.get(1)));
                    case "comment" -> addComment(Integer.parseInt(arguments.get(0)), arguments.get(1));
                    case "wallet" -> getWallet();
                    case "wallet btc" -> getWalletBitcoin();
                }
            }

        }

         CommandSelector commandSelector = new CommandSelector();
         commandSelector.select(command, arguments);

    }

    private void register(String username, char[] password, List<String> tags) {

    }

    private void login(String username, char[] password) {

    }

    private void logout(String username) {

    }

    private void listUsers() {

    }

    private void listFollowers() {

    }

    private void listFollowing() {

    }

    private void followUser(int idUser) {

    }

    private void unfollowUser(int idUser) {

    }

    private void viewBlog() {

    }

    private void createPost(String title, String content) {

    }

    private void showFeed() {

    }

    private void showPost(int idPost) {

    }

    private void deletePost(int idPost) {

    }

    private void rewinPost(int idPost) {

    }

    private void ratePost(int idPost, int vote) {

    }

    private void addComment(int idPost, String comment) {

    }

    private void getWallet() {

    }

    private void getWalletBitcoin() {

    }

    public void close() {

        try {
            socketChannel.close();
        } catch (IOException e) {
            throw new RuntimeException("error while closing the socket");
        }

    }
}
