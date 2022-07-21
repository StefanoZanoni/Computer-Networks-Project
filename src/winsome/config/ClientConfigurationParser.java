package winsome.config;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ClientConfigurationParser extends ConfigurationParser {

    protected void setDefault() {

        host = "localhost";
        tcpPort = 8001;
        try {
            multicastIP = InetAddress.getByName("239.255.32.32");
        } catch (UnknownHostException e) {
            e.printStackTrace(System.err);
            throw new RuntimeException("default multicast host not found");
        }
        multicastPort = 8002;
        rmiCallbackName = "FollowersServerNotification";
        rmiCallbackPort = 8003;
        registerName = "RegistrationWinsomeServer";
        registerPort = 8004;

    }

    @Override
    public void parseConfiguration() {

        Path filepath = Paths.get("client.cfg").toAbsolutePath();

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
            System.err.println("Configuration file not found. Default settings will be applied\n");
            setDefault();
        } catch (IOException e) {
            System.err.println("Error while reading lines from configuration file. Default settings will be applied\n");
            setDefault();
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