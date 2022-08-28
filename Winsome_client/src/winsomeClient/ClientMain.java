package winsomeClient;

import winsomeClient.commands.CommandParser;
import winsomeClient.commands.InvalidCommandException;
import winsomeClient.commands.UnknownCommandException;
import winsomeClient.config.ClientConfigurationParser;
import winsomeClient.multicast.MulticastManager;
import winsomeClient.rmi.ClientRMIManager;
import winsomeClient.shutdown.ClientShutdownHook;
import winsomeClient.tcp.ClientTCPConnectionManager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class ClientMain {

    public static InetAddress multicastIP;
    public static int multicastPort;
    public static String rmiCallbackName;
    public static int rmiCallbackPort;
    public static boolean correctIdentification = false;
    public static boolean error = false;
    public static List<String> followers = new ArrayList<>();

    public static void main(String[] args) {

        ClientConfigurationParser configurationParser = new ClientConfigurationParser();
        try {
            configurationParser.parseConfiguration();
        } catch (UnknownHostException e) {
            e.printStackTrace(System.err);
            System.exit(-1);
        }

        CommandParser commandParser = new CommandParser();

        ClientTCPConnectionManager tcpConnectionManager = null;
        try {
            tcpConnectionManager = new ClientTCPConnectionManager();
        } catch (IOException e) {
            e.printStackTrace(System.err);
            System.exit(-1);
        }
        try {
            tcpConnectionManager.establishConnection(configurationParser.getHost(), configurationParser.getTcpPort());
        } catch (IOException e) {
            e.printStackTrace(System.err);
            System.exit(-1);
        }

        ClientShutdownHook shutdownHook = new ClientShutdownHook(tcpConnectionManager, commandParser);
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        String command = "valid";

        do {

            if (!error)
                System.out.print("> ");

            try {
                commandParser.parse();
            } catch (InvalidCommandException e) {
                System.out.println(e.getMessage());
                System.out.print("> ");
                error = true;
                continue;
            } catch (UnknownCommandException e) {
                System.out.println("< This is not a valid command");
                System.out.print("> ");
                error = true;
                continue;
            }

            command = commandParser.getCommand();

            if (command != null) {

                if (command.compareTo("list followers") == 0) {
                    System.out.println("< " + followers);
                    continue;
                }

                List<String> arguments = commandParser.getArguments();

                tcpConnectionManager.interact(command, arguments);

                if ((command.compareTo("register") == 0 || command.compareTo("login") == 0)
                        && correctIdentification) {

                    MulticastManager multicastManager = null;
                    try {
                        multicastManager = new MulticastManager();
                    } catch (IOException e) {
                        e.printStackTrace(System.err);
                        System.exit(-1);
                    }

                    shutdownHook.setMulticastManager(multicastManager);
                    Thread multicastManagerThread = new Thread(multicastManager);
                    shutdownHook.setMulticastManagerThread(multicastManagerThread);
                    multicastManagerThread.start();
                    System.out.println("< Operation completed successfully");
                    error = false;

                    ClientRMIManager rmiManger = null;
                    try {
                        rmiManger = new ClientRMIManager(arguments.get(0));
                    } catch (RemoteException | NotBoundException e) {
                        e.printStackTrace(System.err);
                        System.exit(-1);
                    }
                    shutdownHook.setRMIManager(rmiManger);
                    try {
                        rmiManger.register();
                    } catch (RemoteException e) {
                        e.printStackTrace(System.err);
                        System.exit(-1);
                    }

                }

            }

        } while( (command != null ? command.compareTo("logout") : 0) != 0 );

        // correct termination has already been set to true or
        // false (in case of error) in which case I can't set it newly to true;
        if (command != null) {
            shutdownHook.setCorrectTermination(true);
            System.exit(0);
        }

    }

}