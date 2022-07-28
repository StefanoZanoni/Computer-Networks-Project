package winsomeClient.shutdown;

import winsomeClient.commands.CommandParser;
import winsomeClient.tcp.ClientTCPConnectionManager;

import java.util.Collections;

public class ClientShutdownHook extends Thread {

    private final ClientTCPConnectionManager clientTCPConnectionManager;
    private final CommandParser commandParser;
    private boolean correctTermination = false;

    public ClientShutdownHook(ClientTCPConnectionManager clientTCPConnectionManager, CommandParser commandParser) {

        this.clientTCPConnectionManager = clientTCPConnectionManager;
        this.commandParser = commandParser;

    }

    public void run() {

        if (!correctTermination)
            clientTCPConnectionManager.interact("logout", Collections.emptyList());

        clientTCPConnectionManager.close();
        commandParser.close();

    }

    public void setCorrectTermination(boolean flag) { correctTermination = flag; }

}