package winsome.config;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 *  This class is used to parse the configuration file of both client and server.
 *  The method parseConfiguration check for all fields for validation and in case of invalid or not present
 *  data load the default configuration.
 */
public abstract class ConfigurationParser {

    protected InetAddress host;
    protected int tcpPort;

    protected Map<String, String> map = new HashMap<>();

    public abstract void parseConfiguration() throws IOException;
    protected abstract void setDefault() throws UnknownHostException;

}