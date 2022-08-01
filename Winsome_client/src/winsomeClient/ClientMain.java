package winsomeClient;

import winsomeClient.commands.CommandParser;
import winsomeClient.commands.UnknownCommandException;
import winsome.config.ClientConfigurationParser;
import winsomeClient.multicast.MulticastManager;
import winsomeClient.shutdown.ClientShutdownHook;
import winsomeClient.tcp.ClientTCPConnectionManager;

import java.net.InetAddress;

public class ClientMain {

    public static InetAddress multicastIP;
    public static int multicastServerPort;

    public static void main(String[] args) {

        ClientConfigurationParser configurationParser = new ClientConfigurationParser();
        configurationParser.parseConfiguration();

        ClientTCPConnectionManager tcpConnectionManager = new ClientTCPConnectionManager();
        tcpConnectionManager.establishConnection(configurationParser.getHost(), configurationParser.getTcpPort());

        CommandParser commandParser = new CommandParser();
        String command = "valid";

        ClientShutdownHook shutdownHook = new ClientShutdownHook(tcpConnectionManager, commandParser);
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        do {

            try {
                commandParser.parse();
            } catch (IllegalArgumentException e) {
                System.err.println("The number of inserted arguments is not valid");
                continue;
            } catch (UnknownCommandException e) {
                System.err.println("This is not a valid command");
                continue;
            }
            command = commandParser.getCommand();
            tcpConnectionManager.interact(command, commandParser.getArguments());
            if (command.compareTo("register") == 0 || command.compareTo("login") == 0) {
                MulticastManager multicastManager = new MulticastManager(configurationParser.getMulticastPort());
                shutdownHook.setMulticastManager(multicastManager);
                Thread multicastManagerThread = new Thread(multicastManager);
                shutdownHook.setMulticastManagerThread(multicastManagerThread);
                multicastManagerThread.start();
                System.out.println("< Operation completed successfully");
            }

        } while(command.compareTo("logout") != 0);

        shutdownHook.setCorrectTermination(true);

        System.exit(0);

    }

}