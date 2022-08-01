package winsomeClient.shutdown;

import winsomeClient.ClientMain;
import winsomeClient.commands.CommandParser;
import winsomeClient.multicast.MulticastManager;
import winsomeClient.tcp.ClientTCPConnectionManager;

import java.util.Collections;

public class ClientShutdownHook extends Thread {

    private final ClientTCPConnectionManager clientTCPConnectionManager;
    private final CommandParser commandParser;
    private MulticastManager multicastManager;
    private Thread multicastManagerThread;
    private boolean correctTermination = false;

    public ClientShutdownHook(ClientTCPConnectionManager clientTCPConnectionManager, CommandParser commandParser) {

        this.clientTCPConnectionManager = clientTCPConnectionManager;
        this.commandParser = commandParser;

    }

    public void setMulticastManager(MulticastManager multicastManager){ this.multicastManager = multicastManager; }
    public void setMulticastManagerThread(Thread multicastManagerThread) { this.multicastManagerThread = multicastManagerThread; }

    public void run() {

        if (!correctTermination && ClientMain.correctIdentification)
            clientTCPConnectionManager.interact("logout", Collections.emptyList());

        clientTCPConnectionManager.close();
        commandParser.close();
        multicastManager.shutdown();
        try {
            multicastManagerThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    public void setCorrectTermination(boolean flag) { correctTermination = flag; }

}