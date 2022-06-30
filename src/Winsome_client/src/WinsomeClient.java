import commands.CommandParser;
import winsome.config.ClientConfigurationParser;

import java.io.IOException;

public class WinsomeClient {

    public static void main() {

        ClientConfigurationParser configurationParser = new ClientConfigurationParser();
        try {
            configurationParser.parseConfiguration("client.cfg");
        } catch (IOException e) {
            throw new RuntimeException("Error while reading lines from file");
        }

        TCPConnectionManager tcpConnectionManager = new TCPConnectionManager();
        tcpConnectionManager.establishConnection(configurationParser.getHost(), configurationParser.getTcpPort());

        CommandParser commandParser = new CommandParser();
        String command;
        do {

            commandParser.parse();
            command = commandParser.getCommand();
            tcpConnectionManager.sendCommand(command, commandParser.getArguments());

        } while(command.compareTo("logout") != 0);

    }
}
