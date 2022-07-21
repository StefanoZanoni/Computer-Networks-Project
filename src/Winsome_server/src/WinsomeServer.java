import winsome.config.ServerConfigurationParser;

import java.io.IOException;
import java.util.concurrent.*;

public class WinsomeServer {

    public static void main() {

        ServerConfigurationParser configurationParser = new ServerConfigurationParser();
        configurationParser.parseConfiguration();

        ExecutorService threadPool = new ThreadPoolExecutor(configurationParser.getCorePoolSize(),
                configurationParser.getMaximumPoolSize(), configurationParser.getKeepAliveTime(),
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(configurationParser.getTaskQueueDimension()));

        ServerTCPConnectionsManager tcpConnectionsManager = new ServerTCPConnectionsManager(threadPool,
                configurationParser.getHost(), configurationParser.getTcpPort() );

        while(true) {
            tcpConnectionsManager.select();
        }
    }

}
