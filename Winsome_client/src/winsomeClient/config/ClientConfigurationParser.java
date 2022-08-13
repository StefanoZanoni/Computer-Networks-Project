package winsomeClient.config;

import winsome.config.ConfigurationParser;
import winsome.config.InvalidConfigurationFileException;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientConfigurationParser extends ConfigurationParser {

    protected void setDefault() throws UnknownHostException {

        host = InetAddress.getLocalHost();
        tcpPort = 7000;

    }

    @Override
    public void parseConfiguration() throws UnknownHostException {

        Path filepath = Paths.get("Winsome_client/client.cfg").toAbsolutePath();

        try ( BufferedReader fileReader = new BufferedReader( new FileReader(filepath.toFile()) ) ) {

            String line;
            while ( (line = fileReader.readLine()) != null ) {
                line = line.trim();
                if ( !line.isEmpty() && !line.startsWith("#") ) {
                    String[] words = line.split("=", 2);
                    // there is a match
                    if (words.length == 2) {
                        String key = words[0].trim();
                        String value = words[1].trim();
                        if (key.compareTo("host") == 0 || key.compareTo("tcp_port") == 0)
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

            try {
                tcpPort = Integer.parseInt(map.get("tcp_port"));
                if (tcpPort > 65535 || tcpPort <= 0)
                    throw new InvalidConfigurationFileException("invalid tcp port");
            } catch (NumberFormatException e) {
                throw new InvalidConfigurationFileException("tcp port not present or invalid");
            }

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

    public InetAddress getHost() { return host; }

    public int getTcpPort() { return tcpPort; }

}