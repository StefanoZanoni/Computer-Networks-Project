package winsomeClient.shutdown;

import winsomeClient.ClientMain;
import winsomeClient.commands.CommandParser;
import winsomeClient.multicast.MulticastManager;
import winsomeClient.rmi.ClientRMIManger;
import winsomeClient.tcp.ClientTCPConnectionManager;

import java.util.Collections;

public class ClientShutdownHook extends Thread {

    private final ClientTCPConnectionManager clientTCPConnectionManager;
    private final CommandParser commandParser;
    private MulticastManager multicastManager = null;
    private Thread multicastManagerThread;
    private ClientRMIManger rmiManger = null;
    private boolean correctTermination = false;

    public ClientShutdownHook(ClientTCPConnectionManager clientTCPConnectionManager, CommandParser commandParser) {

        this.clientTCPConnectionManager = clientTCPConnectionManager;
        this.commandParser = commandParser;

    }

    public void setMulticastManager(MulticastManager multicastManager){ this.multicastManager = multicastManager; }
    public void setMulticastManagerThread(Thread multicastManagerThread) { this.multicastManagerThread = multicastManagerThread; }
    public void setRMIManager(ClientRMIManger rmiManager) { this.rmiManger = rmiManager; }

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

    public void setCorrectTermination(boolean flag) { correctTermination = flag; }

}