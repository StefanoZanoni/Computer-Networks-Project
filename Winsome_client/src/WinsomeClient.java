import commands.CommandParser;
import winsome.config.ClientConfigurationParser;

public class WinsomeClient {

    public static void main() {

        ClientConfigurationParser configurationParser = new ClientConfigurationParser();
        configurationParser.parseConfiguration();

        ClientTCPConnectionManager tcpConnectionManager = new ClientTCPConnectionManager();
        tcpConnectionManager.establishConnection(configurationParser.getHost(), configurationParser.getTcpPort());

        CommandParser commandParser = new CommandParser();
        String command;

        do {

            commandParser.parse();
            command = commandParser.getCommand();
            tcpConnectionManager.interact(command, commandParser.getArguments());

        } while(command.compareTo("logout") != 0);

        commandParser.close();
        tcpConnectionManager.close();

    }
}
