package winsomeServer.shutdown;

import winsomeServer.network.StateWriter;
import winsomeServer.tcp.ServerTCPConnectionsManager;

import java.util.Timer;

public class ServerShutdownHook extends Thread {

    private final ServerTCPConnectionsManager tcpConnectionsManager;
    private final Timer timer;
    private final StateWriter stateWriter;

    public ServerShutdownHook(ServerTCPConnectionsManager tcpConnectionsManager,
                              Timer timer, StateWriter stateWriter) {

        this.tcpConnectionsManager = tcpConnectionsManager;
        this.timer = timer;
        this.stateWriter = stateWriter;

    }

    @Override
    public void run() {

        stateWriter.run();
        timer.cancel();
        tcpConnectionsManager.close();

    }

}