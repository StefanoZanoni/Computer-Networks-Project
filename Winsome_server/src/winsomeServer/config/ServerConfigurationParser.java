package winsomeServer.config;

import winsome.config.ConfigurationParser;
import winsome.config.InvalidConfigurationFileException;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

/**
 * This class is used to read the server configuration file
 */
public class ServerConfigurationParser extends ConfigurationParser {

    private final String[] validKeys = new String[]{ "host", "tcp_port", "multicast_IP", "multicast_port", "RMI_callback_name",
                                      "RMI_callback_port", "corePoolSize", "maximumPoolSize", "keepAliveTime",
                                      "taskQueueDimension", "walletUpdateTime", "authorEarnPercentage",
                                      "usersDirPath", "usersNetworkDirPath", "postsDirPath", "postsNetworkDirPath",
                                      "tagsNetworkDirPath", "removedIDsDirPath" };

    private InetAddress multicastIP;
    private int multicastPort;
    private String rmiCallbackName;
    private int rmiCallbackPort;
    private int corePoolSize;
    private int maximumPoolSize;
    private int keepAliveTime;
    private int taskQueueDimension;
    private int walletUpdateTime;
    private float authorEarnPercentage;
    private String usersDirPath;
    private String usersNetworkDirPath;
    private String postsDirPath;
    private String postsNetworkDirPath;
    private String tagsNetworkDirPath;
    private String removedIDsDirPath;

    protected void setDefault() throws UnknownHostException {

        host = InetAddress.getLocalHost();
        multicastIP = InetAddress.getByName("239.255.32.32");
        tcpPort = 7000;
        multicastPort = 6002;
        rmiCallbackName = "FollowersServerNotification";
        rmiCallbackPort = 6003;
        corePoolSize = 8;
        maximumPoolSize = 64;
        keepAliveTime = 20000;
        taskQueueDimension = 20;
        walletUpdateTime = 60;
        authorEarnPercentage = 75;
        usersDirPath = "Winsome_server/users/";
        usersNetworkDirPath = "Winsome_server/users_network/";
        postsDirPath = "Winsome_server/posts/";
        postsNetworkDirPath = "Winsome_server/posts_network/";
        tagsNetworkDirPath = "Winsome_server/tags_network/";
        removedIDsDirPath = "Winsome_server/remove_IDs/";

    }

    @Override
    public void parseConfiguration() throws UnknownHostException {

        Arrays.sort(validKeys);
        Path filepath = Paths.get("Winsome_server/server.cfg").toAbsolutePath();

        try (BufferedReader fileReader = new BufferedReader( new FileReader(filepath.toFile())) ) {

            String line;
            while ( (line = fileReader.readLine()) != null ) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    String[] words = line.split("=", 2);
                    // there is a match
                    if (words.length == 2) {
                        String key = words[0].trim();
                        String value = words[1].trim();
                        if (Collections.binarySearch(Arrays.asList(validKeys), key) >= 0)
                            map.putIfAbsent(key, value);
                    }
                }
            }

            String hostName = map.get("host");
            if (hostName == null)
                throw new InvalidConfigurationFileException("tcp host not present");
            if (hostName.compareTo("localhost") == 0) {
                try {
                    host = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                    e.printStackTrace(System.err);
                    throw new RuntimeException("localhost not found");
                }
            }
            else {
                try {
                    host = InetAddress.getByName(hostName);
                } catch (UnknownHostException e) {
                    throw new InvalidConfigurationFileException("invalid tcp host");
                }
            }

            String multicastName = map.get("multicast_IP");
            if (multicastName == null)
                throw new InvalidConfigurationFileException("multicast host not present");
            try {
                multicastIP = InetAddress.getByName(multicastName);
            } catch (UnknownHostException e) {
                throw new InvalidConfigurationFileException("invalid multicast host");
            }

            rmiCallbackName = map.get("RMI_callback_name");
            if (rmiCallbackName == null)
                throw new InvalidConfigurationFileException("RMI callback name not present");

            try {
                multicastPort = Integer.parseInt(map.get("multicast_port"));
                if (multicastPort > 65535 || multicastPort <= 0)
                    throw new InvalidConfigurationFileException("invalid multicast port");

                tcpPort = Integer.parseInt(map.get("tcp_port"));
                if (tcpPort > 65535 || tcpPort <= 0)
                    throw new InvalidConfigurationFileException("invalid tcp port");

                rmiCallbackPort = Integer.parseInt(map.get("RMI_callback_port"));
                if (rmiCallbackPort > 65535 || rmiCallbackPort <= 0)
                    throw new InvalidConfigurationFileException("invalid RMI callback port");

                corePoolSize = Integer.parseInt(map.get("corePoolSize"));
                maximumPoolSize = Integer.parseInt(map.get("maximumPoolSize"));
                keepAliveTime = Integer.parseInt(map.get("keepAliveTime"));
                taskQueueDimension = Integer.parseInt(map.get("taskQueueDimension"));
                walletUpdateTime = Integer.parseInt(map.get("walletUpdateTime"));
            } catch (NumberFormatException e) {
                throw new InvalidConfigurationFileException("connections port or server settings not present" +
                                                            " or invalid");
            }

            try {
                authorEarnPercentage = Float.parseFloat(map.get("authorEarnPercentage")) / 100;
                if (authorEarnPercentage <= 0 || authorEarnPercentage > 100)
                    throw new InvalidConfigurationFileException("invalid author earn percentage");
            } catch (NullPointerException | NumberFormatException e) {
                throw new InvalidConfigurationFileException("author earn percentage not present");
            }

            usersDirPath = map.get("usersDirPath");
            if (usersDirPath == null)
                throw new InvalidConfigurationFileException("users directory path not present");
            File file = new File(usersDirPath);
            if (!file.isDirectory())
                throw new InvalidConfigurationFileException("invalid users directory path");

            usersNetworkDirPath = map.get("usersNetworkDirPath");
            if (usersNetworkDirPath == null)
                throw new InvalidConfigurationFileException("users network directory path not present");
            file = new File(usersNetworkDirPath);
            if (!file.isDirectory())
                throw new InvalidConfigurationFileException("invalid users network directory path");

            postsDirPath = map.get("postsDirPath");
            if (postsDirPath == null)
                throw new InvalidConfigurationFileException("posts directory path not present");
            file = new File(postsDirPath);
            if (!file.isDirectory())
                throw new InvalidConfigurationFileException("invalid posts directory path");

            postsNetworkDirPath = map.get("postsNetworkDirPath");
            if (postsNetworkDirPath == null)
                throw new InvalidConfigurationFileException("posts network directory path not present");
            file = new File(postsNetworkDirPath);
            if (!file.isDirectory())
                throw new InvalidConfigurationFileException("invalid posts network directory path");

            tagsNetworkDirPath = map.get("tagsNetworkDirPath");
            if (tagsNetworkDirPath == null)
                throw new InvalidConfigurationFileException("tags network directory path not present");
            file = new File(tagsNetworkDirPath);
            if (!file.isDirectory())
                throw new InvalidConfigurationFileException("invalid tags network directory path");

            removedIDsDirPath = map.get("removedIDsDirPath");
            if (removedIDsDirPath == null)
                throw new InvalidConfigurationFileException("removed ids directory path not present");
            file = new File(removedIDsDirPath);
            if (!file.isDirectory())
                throw new InvalidConfigurationFileException("invalid removed ids directory path");

        }
        catch (FileNotFoundException e) {
            System.err.println("Configuration file not found. Default settings will be applied");
            setDefault();
        } catch (IOException e) {
            System.err.println("Error while reading lines from configuration file. Default settings will be applied");
            setDefault();
        } catch (InvalidConfigurationFileException e) {
            System.err.println("Configuration file is not valid. Default settings will be applied");
            System.err.println(e.getMessage());
            setDefault();
        }

    }

    public String getRmiCallbackName() { return rmiCallbackName; }
    public int getTcpPort() { return tcpPort; }
    public int getRmiCallbackPort() { return rmiCallbackPort; }
    public int getMulticastPort() { return multicastPort; }
    public InetAddress getHost() { return host; }
    public float getAuthorEarnPercentage() { return authorEarnPercentage; }
    public int getCorePoolSize() { return corePoolSize; }
    public int getKeepAliveTime() { return keepAliveTime; }
    public int getMaximumPoolSize() { return maximumPoolSize; }
    public int getTaskQueueDimension() { return taskQueueDimension; }
    public int getWalletUpdateTime() { return walletUpdateTime; }
    public InetAddress getMulticastIP() { return multicastIP; }
    public String getUsersDirPath() { return usersDirPath; }
    public String getUsersNetworkDirPath() { return usersNetworkDirPath; }
    public String getPostsDirPath() { return postsDirPath; }
    public String getPostsNetworkDirPath() { return postsNetworkDirPath; }
    public String getTagsNetworkDirPath() {return tagsNetworkDirPath; }
    public String getRemovedIDsDirPath() { return removedIDsDirPath; }

}