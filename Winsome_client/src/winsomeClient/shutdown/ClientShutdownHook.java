package winsomeClient.shutdown;

import winsomeClient.commands.CommandParser;
import winsomeClient.multicast.MulticastManager;
import winsomeClient.tcp.ClientTCPConnectionManager;

import java.util.Collections;

public class ClientShutdownHook extends Thread {

    private final ClientTCPConnectionManager clientTCPConnectionManager;
    private final CommandParser commandParser;
    private MulticastManager multicastManager;
    private boolean correctTermination = false;

    public ClientShutdownHook(ClientTCPConnectionManager clientTCPConnectionManager, CommandParser commandParser) {

        this.clientTCPConnectionManager = clientTCPConnectionManager;
        this.commandParser = commandParser;

    }

    public void setMulticastManager(MulticastManager multicastManager){ this.multicastManager = multicastManager; }

    public void run() {

        if (!correctTermination)
            clientTCPConnectionManager.interact("logout", Collections.emptyList());

        clientTCPConnectionManager.close();
        commandParser.close();
        multicastManager.shutdown();

    }

    public void setCorrectTermination(boolean flag) { correctTermination = flag; }

}