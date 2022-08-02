package winsome.config;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public abstract class ConfigurationParser {

    protected String host;
    protected int tcpPort;
    protected InetAddress multicastIP;
    protected int multicastPort;
    protected String rmiCallbackName;
    protected int rmiCallbackPort;

    protected Map<String, String> map = new HashMap<>();

    public abstract void parseConfiguration() throws IOException;
    protected abstract void setDefault();

}