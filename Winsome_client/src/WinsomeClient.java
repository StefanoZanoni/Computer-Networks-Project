import winsomeClient.commands.CommandParser;
import winsomeClient.commands.UnknownCommandException;
import winsome.config.ClientConfigurationParser;
import winsomeClient.shutdown.ShutdownHook;
import winsomeClient.tcp.ClientTCPConnectionManager;

public class WinsomeClient {

    public static void main(String[] args) {

        ClientConfigurationParser configurationParser = new ClientConfigurationParser();
        configurationParser.parseConfiguration();

        ClientTCPConnectionManager tcpConnectionManager = new ClientTCPConnectionManager();
        tcpConnectionManager.establishConnection(configurationParser.getHost(), configurationParser.getTcpPort());

        CommandParser commandParser = new CommandParser();
        String command = "valid";

        ShutdownHook shutdownHook = new ShutdownHook(tcpConnectionManager, commandParser);
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

        } while(command.compareTo("logout") != 0);

        shutdownHook.setCorrectTermination(true);

    }

}