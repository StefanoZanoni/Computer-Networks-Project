package winsome.config;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ServerConfigurationParser extends ConfigurationParser {

    private int corePoolSize;
    private int maximumPoolSize;
    private int keepAliveTime;
    private int taskQueueDimension;
    private int walletUpdateTime;
    private int authorEarnPercentage;

    protected void setDefault() {
        host = "localhost";
        tcpPort = 6001;
        try {
            multicastIP = InetAddress.getByName("239.255.32.32");
        } catch (UnknownHostException e) {
            e.printStackTrace(System.err);
            throw new RuntimeException("default multicast host not found");
        }
        multicastPort = 6002;
        rmiCallbackName = "FollowersServerNotification";
        rmiCallbackPort = 6003;
        registerName = "RegistrationWinsomeServer";
        registerPort = 6004;
        corePoolSize = 8;
        maximumPoolSize = 64;
        keepAliveTime = 20000;
        taskQueueDimension = 20;
        walletUpdateTime = 60;
        authorEarnPercentage = 75;
    }

    @Override
    public void parseConfiguration() {

        Path filepath = Paths.get("Winsome_server/server.cfg").toAbsolutePath();

        try (BufferedReader  fileReader = new BufferedReader(new FileReader(filepath.toFile()))) {

            String line;
            while (true) {

                try {
                    if ((line = fileReader.readLine()) == null) break;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    String[] words = line.split("=", 2);
                    map.putIfAbsent(words[0].trim(), words[1].trim());
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
            keepAliveTime = Integer.parseInt(map.get("keepAliveTime"));
            taskQueueDimension = Integer.parseInt(map.get("taskQueueDimension"));
            walletUpdateTime = Integer.parseInt(map.get("walletUpdateTime"));
            authorEarnPercentage = Integer.parseInt(map.get("authorEarnPercentage"));

        }
        catch (FileNotFoundException e) {
            System.err.println("Configuration file not found. Default settings will be applied");
            setDefault();
        } catch (UnknownHostException e) {
            throw new RuntimeException("multicast address not found");
        } catch (IOException e) {
            System.err.println("Error while reading lines from configuration file");
            setDefault();
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