package com.testing.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * Reads configuration from specified configuration file
 */
public class ConfigurationReader
{

    private static final Logger LOGGER = LogManager.getLogger(ConfigurationReader.class);

    /**
     * Load configuration properties.
     *
     * @param pathName the path to configuration file
     * @return the properties with all configuration values
     */
    public static Properties loadConfiguration(String pathName)
    {
        File configFile = new File(pathName);
        Properties props = null;

        try
        {
            FileReader reader = new FileReader(configFile);
            props = new Properties();
            props.load(reader);
            reader.close();
        } catch (FileNotFoundException e)
        {
            LOGGER.error("Configuration file not exists.", e);
        } catch (IOException ex)
        {
            LOGGER.error("Cannot read the configuration file.", ex);
        }
        return props;
    }
}
