import winsome.config.ServerConfigurationParser;
import winsomeServer.network.RewardsCalculator;
import winsomeServer.network.StateLoader;
import winsomeServer.network.StateWriter;
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
        timer.scheduleAtFixedRate(new RewardsCalculator(), 1000, configurationParser.getWalletUpdateTime() * 1000L);
        timer.scheduleAtFixedRate(new StateWriter(statePaths), 1000, 10000);

        while(true) {
            tcpConnectionsManager.select();
        }
    }

}