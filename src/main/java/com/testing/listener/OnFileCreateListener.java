package com.testing.listener;

import com.testing.config.Configuration;
import com.testing.handler.XmlFileHandler;
import com.testing.handler.CustomXmlToDBHandler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * The listener which handle newly created file using thread pool
 */
public class OnFileCreateListener implements OnFileChangeListener
{
    private ScheduledExecutorService executor;

    /**
     * Instantiates a new On file create listener with a thread pool that can schedule XmlFileHandler
     * on file creates to run after a given delay
     */
    public OnFileCreateListener()
    {
        executor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() + 1);
    }

    @Override
    public void onFileChange(String filePath)
    {
        executor.submit(new XmlFileHandler(new CustomXmlToDBHandler(), filePath, Configuration.getProcessedFilesFolder(), Configuration.getCorruptedFilesFolder()));
    }
}
