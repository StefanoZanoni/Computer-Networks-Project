package winsomeServer;

import winsomeServer.config.ServerConfigurationParser;
import winsomeServer.network.RewardsCalculator;
import winsomeServer.network.SocialNetworkManager;
import winsomeServer.network.StateLoader;
import winsomeServer.network.StateWriter;
import winsomeServer.rmi.ServerRMIManager;
import winsomeServer.shutdown.ServerShutdownHook;
import winsomeServer.tcp.ServerTCPConnectionsManager;

import java.net.InetAddress;
import java.util.Timer;
import java.util.concurrent.*;

public class ServerMain {

    public static InetAddress multicastIP;
    public static int multicastPort;

    public static String rmiCallbackName;
    public static int rmiCallbackPort;

    public static void main(String[] args) {

        ServerConfigurationParser configurationParser = new ServerConfigurationParser();
        configurationParser.parseConfiguration();

        multicastIP = configurationParser.getMulticastIP();
        multicastPort = configurationParser.getMulticastPort();
        rmiCallbackName = configurationParser.getRmiCallbackName();
        rmiCallbackPort = configurationParser.getRmiCallbackPort();

        ServerRMIManager rmiManager = new ServerRMIManager();
        rmiManager.createRegistry();
        SocialNetworkManager.rmiManager = rmiManager;

        String[] statePaths = new String[]{ configurationParser.getUsersDirPath(),
                                            configurationParser.getUsersNetworkDirPath(),
                                            configurationParser.getPostsDirPath(),
                                            configurationParser.getPostsNetworkDirPath(),
                                            configurationParser.getTagsNetworkDirPath(),
                                            configurationParser.getRemovedIDsDirPath() };

        StateLoader stateLoader = new StateLoader(statePaths);
        stateLoader.loadState();

        ExecutorService threadPool = new ThreadPoolExecutor(configurationParser.getCorePoolSize(),
                configurationParser.getMaximumPoolSize(), configurationParser.getKeepAliveTime(),
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(configurationParser.getTaskQueueDimension()));

        ServerTCPConnectionsManager tcpConnectionsManager = new ServerTCPConnectionsManager(threadPool,
                configurationParser.getHost(), configurationParser.getTcpPort() );

        Timer timer = new Timer();

        RewardsCalculator rewardsCalculator = new RewardsCalculator(configurationParser.getAuthorEarnPercentage());
        timer.scheduleAtFixedRate(rewardsCalculator, 1000, configurationParser.getWalletUpdateTime() * 1000L);

        StateWriter stateWriter = new StateWriter(statePaths);
        timer.scheduleAtFixedRate(stateWriter, 1000, 10000);

        ServerShutdownHook shutdownHook = new ServerShutdownHook(tcpConnectionsManager, timer,
                                                                    stateWriter, rewardsCalculator, rmiManager);
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        tcpConnectionsManager.select();

    }

}