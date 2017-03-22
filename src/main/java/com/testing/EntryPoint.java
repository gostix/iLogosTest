package com.testing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * The type Entry point.
 */
public class EntryPoint
{

    private static final Logger LOGGER = LogManager.getLogger(EntryPoint.class);

    /**
     * The entry point of application.
     *
     * @param args the input arguments which can specifies configuration file path
     */
    public static void main(String[] args)
    {
        FolderMonitor folderMonitor;
            if (args.length == 2 && (args[0].equals("-c") || args[0].equals("--config")))
            {
                LOGGER.debug("Application was started using " + new File(args[1]).getAbsolutePath() + " configuration file.");
                folderMonitor = new FolderMonitor(args[1]);
            }
            else
            {
               LOGGER.error("Please specify configuration file path. " +
                       "Supports only '-c' or '--config' to specifies configuration file path");
               return;
            }
        folderMonitor.startMonitor();
    }
}
