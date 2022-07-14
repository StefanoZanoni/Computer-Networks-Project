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

        ExecutorService threadPool = new ThreadPoolExecutor(configurationParser.getCorePoolSize(),
                configurationParser.getMaximumPoolSize(), configurationParser.getKeepAliveTime(),
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(configurationParser.getTaskQueueDimension()));

        ServerTCPConnectionsManager connectionsManager = new ServerTCPConnectionsManager(threadPool);

    }

}
