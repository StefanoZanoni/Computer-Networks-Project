package winsomeServer;

import winsomeServer.config.ServerConfigurationParser;
import winsomeServer.network.RewardsCalculator;
import winsomeServer.network.SocialNetworkManager;
import winsomeServer.network.StateLoader;
import winsomeServer.network.StateWriter;
import winsomeServer.rmi.ServerRMIManager;
import winsomeServer.shutdown.ServerShutdownHook;
import winsomeServer.tcp.ServerTCPConnectionsManager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.util.Timer;
import java.util.concurrent.*;

public class ServerMain {

    public static InetAddress multicastIP;
    public static int multicastPort;
    public static String rmiCallbackName;
    public static int rmiCallbackPort;

    public static void main(String[] args) {

        ServerConfigurationParser configurationParser = new ServerConfigurationParser();
        try {
            configurationParser.parseConfiguration();
        } catch (UnknownHostException e) {
            e.printStackTrace(System.err);
            System.exit(-1);
        }

        multicastIP = configurationParser.getMulticastIP();
        multicastPort = configurationParser.getMulticastPort();
        rmiCallbackName = configurationParser.getRmiCallbackName();
        rmiCallbackPort = configurationParser.getRmiCallbackPort();

        String[] statePaths = new String[]{ configurationParser.getUsersDirPath(),
                configurationParser.getUsersNetworkDirPath(),
                configurationParser.getPostsDirPath(),
                configurationParser.getPostsNetworkDirPath(),
                configurationParser.getTagsNetworkDirPath(),
                configurationParser.getRemovedIDsDirPath() };

        StateLoader stateLoader = new StateLoader(statePaths);
        try {
            stateLoader.loadState();
        } catch (IOException e) {
            e.printStackTrace(System.err);
            System.exit(-1);
        }

        ServerRMIManager rmiManager = null;
        try {
            rmiManager = new ServerRMIManager();
        } catch (RemoteException e) {
            e.printStackTrace(System.err);
            System.exit(-1);
        }
        try {
            rmiManager.createRegistry();
        } catch (RemoteException | AlreadyBoundException e) {
            e.printStackTrace(System.err);
            System.exit(-1);
        }
        SocialNetworkManager.rmiManager = rmiManager;

        Timer timer = new Timer();

        RewardsCalculator rewardsCalculator = null;
        try {
            rewardsCalculator = new RewardsCalculator(configurationParser.getAuthorEarnPercentage());
        } catch (IOException e) {
            e.printStackTrace(System.err);
            System.exit(-1);
        }
        timer.scheduleAtFixedRate(rewardsCalculator, 1000, configurationParser.getWalletUpdateTime() * 1000L);

        StateWriter stateWriter = null;
        try {
            stateWriter = new StateWriter(statePaths);
        } catch (IOException e) {
            e.printStackTrace(System.err);
            System.exit(-1);
        }
        timer.scheduleAtFixedRate(stateWriter, 1000, 10000);

        ExecutorService threadPool = new ThreadPoolExecutor(configurationParser.getCorePoolSize(),
                configurationParser.getMaximumPoolSize(), configurationParser.getKeepAliveTime(),
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(configurationParser.getTaskQueueDimension()));

        ServerTCPConnectionsManager tcpConnectionsManager = null;
        try {
            tcpConnectionsManager = new ServerTCPConnectionsManager(threadPool,
                    configurationParser.getHost(), configurationParser.getTcpPort() );
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        ServerShutdownHook shutdownHook = new ServerShutdownHook(tcpConnectionsManager, timer,
                                                                    stateWriter, rewardsCalculator, rmiManager);
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        tcpConnectionsManager.select();

    }

}