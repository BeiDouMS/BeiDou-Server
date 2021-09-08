package config;

import com.esotericsoftware.yamlbeans.YamlReader;
import constants.string.CharsetConstants;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;


public class YamlConfig {
    private static final String CONFIG_FILE_NAME = "config.yaml";
    public static final YamlConfig config = loadConfig(CONFIG_FILE_NAME);
    
    public List<WorldConfig> worlds;
    public ServerConfig server;

    private static YamlConfig loadConfig(String fileName) {
        try {
            YamlReader reader = new YamlReader(new FileReader(fileName, CharsetConstants.CHARSET));
            YamlConfig config = reader.read(YamlConfig.class);
            reader.close();
            return config;
        } catch (IOException e) {
            throw new RuntimeException(getLoadConfigErrorMessage(e));
        }
    }

    private static String getLoadConfigErrorMessage(IOException e) {
        if (e instanceof FileNotFoundException) {
            return "Could not read config file " + CONFIG_FILE_NAME + ": " + e.getMessage();
        }

        return "Could not successfully parse config file " + CONFIG_FILE_NAME + ": " + e.getMessage();
    }

    public static String loadCharset() {
        try {
            YamlReader reader = new YamlReader(new FileReader(CONFIG_FILE_NAME, StandardCharsets.US_ASCII));
            reader.getConfig().readConfig.setIgnoreUnknownProperties(true);
            StrippedYamlConfig charsetConfig = reader.read(StrippedYamlConfig.class);
            reader.close();
            return charsetConfig.server.CHARSET;
        } catch (IOException e) {
            throw new RuntimeException(getLoadConfigErrorMessage(e));
        }
    }

    private static class StrippedYamlConfig {
        public StrippedServerConfig server;

        private static class StrippedServerConfig {
            public String CHARSET;
        }
    }
}
