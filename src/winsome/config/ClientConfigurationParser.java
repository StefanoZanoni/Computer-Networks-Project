package winsome.config;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ClientConfigurationParser implements ConfigurationParser {

    String host = "localhost";
    int tcpPort = 8001;
    InetAddress multicastIP;
    int  multicastPort = 8002;
    String rmiCallbackName = "FollowersServerNotification";
    int rmiCallbackPort = 8003;
    String registerName = "RegistrationWinsomeServer";
    int registerPort = 8004;
    Map<String, String> map = new HashMap<>();

    public ClientConfigurationParser() {

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

        try ( BufferedReader fileReader = new BufferedReader(new FileReader(filepath.toFile())) ) {

            String line;
            while ( (line = fileReader.readLine()) != null ) {
                line = line.trim();
                if ( !line.isEmpty() && !line.startsWith("#")) {
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

        }
        catch (FileNotFoundException e) {
            e.printStackTrace(System.err);
            throw new RuntimeException("configuration file not found");
        }

    }

    public String getHost() { return host; }

    public int getMulticastPort() { return multicastPort; }

    public int getRegisterPort() { return registerPort; }

    public int getRmiCallbackPort() { return rmiCallbackPort; }

    public int getTcpPort() { return tcpPort; }

    public String getRegisterName() { return registerName; }

    public String getRmiCallbackName() { return rmiCallbackName; }

    public InetAddress getMulticastIP() { return multicastIP; }

}