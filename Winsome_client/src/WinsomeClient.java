import commands.CommandParser;
import commands.UnknownCommandException;
import winsome.config.ClientConfigurationParser;

import javax.sound.midi.SysexMessage;
import java.awt.datatransfer.SystemFlavorMap;

public class WinsomeClient {

    public static void main(String[] args) {

        ClientConfigurationParser configurationParser = new ClientConfigurationParser();
        configurationParser.parseConfiguration();

        ClientTCPConnectionManager tcpConnectionManager = new ClientTCPConnectionManager();
        tcpConnectionManager.establishConnection(configurationParser.getHost(), configurationParser.getTcpPort());

        CommandParser commandParser = new CommandParser();
        String command = "valid";

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

        commandParser.close();
        tcpConnectionManager.close();

    }
}