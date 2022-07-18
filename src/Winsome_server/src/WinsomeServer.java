import winsome.config.ServerConfigurationParser;

import java.io.IOException;
import java.util.concurrent.*;

public class WinsomeServer {

    public static void main() {

        ServerConfigurationParser configurationParser = new ServerConfigurationParser();
        try {
            configurationParser.parseConfiguration();
        } catch (IOException e) {
            throw new RuntimeException("Error while reading lines from configuration file");
        }

        SocialNetworkManager socialNetworkManager = new SocialNetworkManager();

        ExecutorService threadPool = new ThreadPoolExecutor(configurationParser.getCorePoolSize(),
                configurationParser.getMaximumPoolSize(), configurationParser.getKeepAliveTime(),
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(configurationParser.getTaskQueueDimension()));

        ServerTCPConnectionsManager tcpConnectionsManager = new ServerTCPConnectionsManager( threadPool,
                configurationParser.getHost(), configurationParser.getTcpPort() );

        while(true) {
            tcpConnectionsManager.select();
        }
    }

}
