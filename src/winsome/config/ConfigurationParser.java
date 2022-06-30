package winsome.config;

import java.io.IOException;

public interface ConfigurationParser {
    void parseConfiguration(String filename) throws IOException;

}
