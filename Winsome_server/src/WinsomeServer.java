import winsome.config.ServerConfigurationParser;
import winsomeServer.network.RewardsCalculator;
import winsomeServer.network.StateLoader;
import winsomeServer.network.StateWriter;
import winsomeServer.shutdown.ServerShutdownHook;
import winsomeServer.tcp.ServerTCPConnectionsManager;

import java.util.Timer;
import java.util.concurrent.*;

public class WinsomeServer {

    public static void main(String[] args) {

        ServerConfigurationParser configurationParser = new ServerConfigurationParser();
        configurationParser.parseConfiguration();

        String[] statePaths = new String[]{ configurationParser.getUsersDirPath(),
                                            configurationParser.getUsersNetworkDirPath(),
                                            configurationParser.getPostsDirPath(),
                                            configurationParser.getPostsNetworkDirPath(),
                                            configurationParser.getTagsNetworkDirPath() };

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

        ServerShutdownHook shutdownHook = new ServerShutdownHook(tcpConnectionsManager, timer, stateWriter);
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        tcpConnectionsManager.select();

    }

}