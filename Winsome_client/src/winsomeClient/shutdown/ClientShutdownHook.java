package winsomeClient.shutdown;

import winsomeClient.ClientMain;
import winsomeClient.commands.CommandParser;
import winsomeClient.multicast.MulticastManager;
import winsomeClient.rmi.ClientRMIManager;
import winsomeClient.tcp.ClientTCPConnectionManager;

import java.util.Collections;

public class ClientShutdownHook extends Thread {

    private final ClientTCPConnectionManager clientTCPConnectionManager;
    private final CommandParser commandParser;
    private MulticastManager multicastManager = null;
    private Thread multicastManagerThread;
    private ClientRMIManager rmiManger = null;
    private boolean correctTermination = false;

    public ClientShutdownHook(ClientTCPConnectionManager clientTCPConnectionManager, CommandParser commandParser) {

        this.clientTCPConnectionManager = clientTCPConnectionManager;
        this.commandParser = commandParser;

    }

    public void setMulticastManager(MulticastManager multicastManager){ this.multicastManager = multicastManager; }
    public void setMulticastManagerThread(Thread multicastManagerThread) { this.multicastManagerThread = multicastManagerThread; }
    public void setRMIManager(ClientRMIManager rmiManager) { this.rmiManger = rmiManager; }
    public void setCorrectTermination(boolean flag) { correctTermination = flag; }

    public void run() {

        if (!correctTermination && ClientMain.correctIdentification)
            clientTCPConnectionManager.interact("logout", Collections.emptyList());

        commandParser.close();
        if (clientTCPConnectionManager != null)
            clientTCPConnectionManager.close();
        if (multicastManager != null)
            multicastManager.shutdown();
        if (multicastManagerThread != null)
            try {
                multicastManagerThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
        }
        if (rmiManger != null)
            rmiManger.unregister();

    }

}