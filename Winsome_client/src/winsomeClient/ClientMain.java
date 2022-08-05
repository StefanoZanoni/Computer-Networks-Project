package winsomeClient;

import winsomeClient.commands.CommandParser;
import winsomeClient.commands.UnknownCommandException;
import winsomeClient.config.ClientConfigurationParser;
import winsomeClient.multicast.MulticastManager;
import winsomeClient.rmi.ClientRMIManger;
import winsomeClient.shutdown.ClientShutdownHook;
import winsomeClient.tcp.ClientTCPConnectionManager;

import java.net.InetAddress;
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
        configurationParser.parseConfiguration();

        CommandParser commandParser = new CommandParser();

        ClientTCPConnectionManager tcpConnectionManager = new ClientTCPConnectionManager();
        tcpConnectionManager.establishConnection(configurationParser.getHost(), configurationParser.getTcpPort());

        ClientShutdownHook shutdownHook = new ClientShutdownHook(tcpConnectionManager, commandParser);
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        String command = "valid";

        do {

            if (!error) {
                System.out.print("> ");
                System.out.flush();
            }

            // sleep is necessary to synchronize System.err and System.out buffer
            try {
                commandParser.parse();
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
                error = true;
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                System.out.print("> ");
                continue;
            } catch (UnknownCommandException e) {
                System.err.println("< This is not a valid command");
                error = true;
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                System.out.print("> ");
                continue;
            }

            command = commandParser.getCommand();
            if (command.compareTo("list followers") == 0) {
                System.out.println(followers);
                continue;
            }
            List<String> arguments = commandParser.getArguments();

            tcpConnectionManager.interact(command, arguments);

            if ( (command.compareTo("register") == 0 || command.compareTo("login") == 0)
                    && correctIdentification) {

                MulticastManager multicastManager = new MulticastManager();
                shutdownHook.setMulticastManager(multicastManager);
                Thread multicastManagerThread = new Thread(multicastManager);
                shutdownHook.setMulticastManagerThread(multicastManagerThread);
                multicastManagerThread.start();
                System.out.println("< Operation completed successfully");
                error = false;

                ClientRMIManger rmiManger = new ClientRMIManger(arguments.get(0));
                shutdownHook.setRMIManager(rmiManger);
                rmiManger.register();

            }

        } while(command.compareTo("logout") != 0);

        shutdownHook.setCorrectTermination(true);

        System.exit(0);

    }

}