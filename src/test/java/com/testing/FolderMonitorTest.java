package com.testing;

import org.junit.Test;

public class FolderMonitorTest
{
    @Test
    public void startMonitor() throws Exception
    {
        String config = "src/test/resources/config.properties";
        new FolderMonitor(config).startMonitor();
        Thread.sleep(25000);
    }

}