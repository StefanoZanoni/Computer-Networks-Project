package winsomeServer.shutdown;

import winsomeServer.network.RewardsCalculator;
import winsomeServer.network.StateWriter;
import winsomeServer.rmi.ServerRMIManager;
import winsomeServer.tcp.ServerTCPConnectionsManager;

import java.io.IOException;
import java.util.Timer;

public class ServerShutdownHook extends Thread {

    private final ServerTCPConnectionsManager tcpConnectionsManager;
    private final Timer timer;
    private final StateWriter stateWriter;
    private final RewardsCalculator rewardsCalculator;
    private final ServerRMIManager rmiManager;

    public ServerShutdownHook(ServerTCPConnectionsManager tcpConnectionsManager,
                              Timer timer, StateWriter stateWriter,
                              RewardsCalculator rewardsCalculator, ServerRMIManager rmiManager) {

        this.tcpConnectionsManager = tcpConnectionsManager;
        this.timer = timer;
        this.stateWriter = stateWriter;
        this.rewardsCalculator = rewardsCalculator;
        this.rmiManager = rmiManager;
    }

    @Override
    public void run() {

        stateWriter.run();
        timer.cancel();
        try {
            tcpConnectionsManager.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            rewardsCalculator.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        rmiManager.unbind();

    }

}