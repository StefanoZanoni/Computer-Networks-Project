package winsome.config;

import javax.swing.event.InternalFrameEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ServerConfigurationParser implements ConfigurationParser {

    String host = "localhost";
    int tcpPort = 6001;
    InetAddress multicastIP;
    int multicastPort = 6002;
    String rmiCallbackName = "FollowersServerNotification";
    int rmiCallbackPort = 6003;
    String registerName = "RegistrationWinsomeServer";
    int registerPort = 6004;
    int corePoolSize = 2;
    int maximumPoolSize = 64;
    int keepAliveTime = 20000;
    int taskQueueDimension = 10;
    int walletUpdateTime = 60;
    int authorEarnPercentage = 75;

    Map<String, String> map = new HashMap<>();

    ServerConfigurationParser() {

        try {
            multicastIP = InetAddress.getByName("239.255.32.32");
        } catch (UnknownHostException e) {
            e.printStackTrace(System.err);
            throw new RuntimeException("default multicast host not found");
        }

    }

    @Override
    public void parseConfiguration(String filename) throws IOException {

        Path filepath = Paths.get(filename).toAbsolutePath();

        try (BufferedReader  fileReader = new BufferedReader(new FileReader(filepath.toFile()))) {

            String line;
            while ((line = fileReader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    String[] words = line.split("=", 2);
                    map.putIfAbsent(words[0], words[1]);
                }
            }
            host = map.get("host");
            tcpPort = Integer.parseInt(map.get("tcp_port"));
            multicastIP = InetAddress.getByName(map.get("multicast_IP"));
            multicastPort = Integer.parseInt(map.get("multicast_port"));
            rmiCallbackName = map.get("RMI_callback_name");
            rmiCallbackPort = Integer.parseInt(map.get("RMI_callback_port"));
            registerName = map.get("register_name");
            registerPort = Integer.parseInt(map.get("register_port"));
            corePoolSize = Integer.parseInt(map.get("corePoolSize"));
            maximumPoolSize = Integer.parseInt(map.get("maximumPoolSize"));
            keepAliveTime = Integer.parseInt(map.get("keepAliceTime"));
            taskQueueDimension = Integer.parseInt(map.get("taskQueueDimension"));
            walletUpdateTime = Integer.parseInt(map.get("walletUpdateTime"));
            authorEarnPercentage = Integer.parseInt(map.get("authorEarnPercentage"));

        }
        catch (FileNotFoundException e) {
            e.printStackTrace(System.err);
            throw new RuntimeException("server configuration file not found");
        }

    }

    public String getRmiCallbackName() { return rmiCallbackName; }

    public String getRegisterName() { return registerName; }

    public int getTcpPort() { return tcpPort; }

    public int getRmiCallbackPort() { return rmiCallbackPort; }

    public int getRegisterPort() { return registerPort; }

    public int getMulticastPort() { return multicastPort; }

    public String getHost() { return host; }

    public int getAuthorEarnPercentage() { return authorEarnPercentage; }

    public int getCorePoolSize() { return corePoolSize; }

    public int getKeepAliveTime() { return keepAliveTime; }

    public int getMaximumPoolSize() { return maximumPoolSize; }

    public int getTaskQueueDimension() { return taskQueueDimension; }

    public int getWalletUpdateTime() { return walletUpdateTime; }

    public InetAddress getMulticastIP() { return multicastIP; }

}