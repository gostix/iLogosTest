package com.testing;

import com.testing.config.Configuration;
import com.testing.handler.XmlFileHandler;
import com.testing.listener.OnFileCreateListener;
import com.testing.services.SimpleDirectoryWatchService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This class monitoring the specified directory for the presence of files of a certain XML-format.
 * A file of the appropriate format, the application saves its contents to the
 * Similar in structure to the DB table and moves the file to the directory for processed files.
 */
class FolderMonitor
{
    private static final Logger LOGGER = LogManager.getLogger(FolderMonitor.class);

    private String configurationFilePath;

    /**
     * Instantiates a new Folder monitor with specified configuration file name.
     *
     * @param configurationFilePath the configuration file path
     */
    FolderMonitor(String configurationFilePath)
    {
        this.configurationFilePath = configurationFilePath;
    }

    /**
     * Start tracking folder
     */
    void startMonitor()
    {
        if (!Configuration.initConfiguration(configurationFilePath))
        {
            LOGGER.error("Typos in configuration file. The program will be stopped.");
            return;
        }
        String watchDirectory = Configuration.getMonitoringFolderPath();
        LOGGER.debug("Watching >>" + watchDirectory + "<< directory.");
        String filePattern = Configuration.getFileNamePattern() + ".xml";
        SimpleDirectoryWatchService watchService;
        try
        {
            watchService = new SimpleDirectoryWatchService(new OnFileCreateListener(), watchDirectory, filePattern);
        } catch (IOException | IllegalArgumentException e)
        {
            LOGGER.error(e);
            return;
        }
        long delay = Configuration.getMonitoringPeriod();
        TimeUnit timeUnit = Configuration.getMonitoringTimeUnit();

        if (timeUnit != null)
        {
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            executor.scheduleWithFixedDelay(watchService, 0, delay, timeUnit);
        }
        else
        {
            LOGGER.error("Cannot start FolderMonitor, please check configuration file.");
            return;
        }
        restore(watchDirectory);
    }


    /**
     * Restores files from last unsuccessful writing to DB.
     *
     * @param monitoringPath the folder where to find files to restore
     */
    private void restore(String monitoringPath)
    {
        File monitoringFolder = new File(monitoringPath);
        File[] monitoringFolderFiles = monitoringFolder.listFiles();
        if (monitoringFolderFiles != null)
        {
            Optional<File> optional = Arrays
                    .stream(monitoringFolderFiles)
                    .filter(file -> XmlFileHandler.NOT_RECORDED_FILES_FOLDER.equals(file.getName()))
                    .findFirst();

            if (optional.isPresent())
            {
                File restoreFolder = optional.get();
                File[] restoreFolderFiles = restoreFolder.listFiles();
                if (restoreFolderFiles != null)
                {
                    for (File fileToRestore : restoreFolderFiles)
                    {
                        try
                        {
                            Files.move(Paths.get(fileToRestore.toURI()), Paths.get(monitoringPath + File.separator + fileToRestore.getName()));
                        } catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    if (!restoreFolder.delete())
                    {
                        LOGGER.debug("Restore folder was not deleted.");
                    }
                }
            }
        }
    }
}
