package com.testing.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.testing.config.Configuration.PropertiesEnum.*;

/**
 * Class represents configuration parsed properties file
 */
public final class Configuration
{
    private static final Logger LOGGER = LogManager.getLogger(Configuration.class);
    private static Properties properties;

    /**
     * The enum configuration properties enum.
     */
    enum PropertiesEnum
    {
        JDBC_DRIVER_NAME,
        DB_URL,
        DB_USER_NAME,
        USER_PASSWORD,
        DB_TABLE_NAME,
        MONITORING_FOLDER,
        FILE_NAME_PATTERN,
        MONITORING_PERIOD,
        PROCESSED_FILES_FOLDER,
        CORRUPTED_FILES_FOLDER
    }

    /**
     * Init configuration.
     *
     * @param configurationPath the configuration file path
     * @return the boolean if configuration loaded without errors
     */
    public static boolean initConfiguration(String configurationPath)
    {
        File configurationFile = new File(configurationPath);
        properties = ConfigurationReader.loadConfiguration(configurationFile.getAbsolutePath());
        return validateConfiguration();
    }

    /**
     * Gets jdbc driver name.
     *
     * @return the jdbc driver name
     */
    public static String getJDBCDriverName()
    {
        return properties.getProperty(JDBC_DRIVER_NAME.name());
    }

    /**
     * Gets DBurl.
     *
     * @return the DBurl
     */
    public static String getDBurl()
    {
        return properties.getProperty(DB_URL.name());
    }

    /**
     * Gets db user name.
     *
     * @return the db user name
     */
    public static String getDBUserName()
    {
        return properties.getProperty(DB_USER_NAME.name());
    }

    /**
     * Gets password.
     *
     * @return the password
     */
    public static String getPassword()
    {
        return properties.getProperty(USER_PASSWORD.name());
    }

    /**
     * Gets db table.
     *
     * @return the db table
     */
    public static String getDBTable()
    {
        return properties.getProperty(DB_TABLE_NAME.name());
    }

    /**
     * Gets monitoring folder path.
     *
     * @return the monitoring folder path
     */
    public static String getMonitoringFolderPath()
    {
        return properties.getProperty(MONITORING_FOLDER.name());
    }

    /**
     * Gets file name pattern.
     *
     * @return the file name pattern
     */
    public static String getFileNamePattern()
    {
        return properties.getProperty(FILE_NAME_PATTERN.name());
    }

    /**
     * Gets monitoring period.
     *
     * @return the monitoring period
     */
    public static Long getMonitoringPeriod()
    {
        Long period = null;
        try
        {
            period = Long.valueOf(getMonitoringPeriodValues()[0]);
        } catch (NumberFormatException e)
        {
            LOGGER.error("Cannot read " + MONITORING_PERIOD.name() + " value from configuration. It must be integer number.");
        }
        return period;
    }

    /**
     * Gets monitoring time unit.
     *
     * @return the monitoring time unit
     */
    public static TimeUnit getMonitoringTimeUnit()
    {
        String timeUnitValue = getMonitoringPeriodValues()[1];
        if (timeUnitValue != null)
        {
            return TimeUnit.valueOf(timeUnitValue.toUpperCase());
        }
        return null;
    }

    /**
     * Returns values from monitoring period properties and check correction.
     *
     * @return Strings array which contains monitoring period and time unit.
     * If values is incorrect returns default value '5 SECONDS'.
     */
    private static String[] getMonitoringPeriodValues()
    {
        String propertyValue = properties.getProperty(MONITORING_PERIOD.name());
        String[] defaultValue = {"5", "SECONDS"};
        if(propertyValue.length() != 0)
        {
            String[] monitoringPeriodValues = propertyValue.split(" ");
            if (monitoringPeriodValues.length != 2)
            {
                LOGGER.error("Cannot read " + MONITORING_PERIOD.name() + " property from configuration.");
                return defaultValue;
            }
            LOGGER.info(MONITORING_PERIOD.name() + " has incorrect format. Will set to 5 SECONDS");
            return monitoringPeriodValues;
        }
        LOGGER.info(MONITORING_PERIOD.name() + " not specified. Will set to 5 SECONDS");
       return defaultValue;
    }

    /**
     * Gets processed files folder.
     *
     * @return the processed files folder
     */
    public static String getProcessedFilesFolder()
    {
        String value = properties.getProperty(PROCESSED_FILES_FOLDER.name());
        if(value.length() == 0)
        {
            return getMonitoringFolderPath() + File.separator + "processed";
        }
        return value;
    }

    /**
     * Gets corrupted files folder.
     *
     * @return the corrupted files folder
     */
    public static String getCorruptedFilesFolder()
    {
        String value = properties.getProperty(CORRUPTED_FILES_FOLDER.name());
        if(value.length() == 0)
        {
            return getMonitoringFolderPath() + File.separator + "corrupted";
        }
        return value;
    }


    /**
     * Validate configuration file properties for containing all values from {@link PropertiesEnum}
     *
     * @return true if all properties exists
     */
    private static boolean validateConfiguration()
    {
        Set<String> propertiesNames = properties.stringPropertyNames();
        for (PropertiesEnum key : PropertiesEnum.values())
        {
            if (!propertiesNames.contains(key.name()))
            {
                LOGGER.error("Cannot find " + key.name() + " property in configuration file.");
                return false;
            }
        }
        return true;
    }
}
