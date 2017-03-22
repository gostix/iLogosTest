package com.testing.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.Instant;

/**
 * The Xml file handler that read and parse xml file using dedicated thread.
 */
public class XmlFileHandler implements Runnable
{

    private static final Logger LOGGER = LogManager.getLogger(XmlFileHandler.class);

    private String filePath;
    private XmlToDBHandler handler;
    private String processedFilesFolder;
    private String corruptedFilesFolder;
    private static final SAXParserFactory factory = SAXParserFactory.newInstance();

    /**
     * The constant NOT_RECORDED_FILES_FOLDER.
     */
    public static final String NOT_RECORDED_FILES_FOLDER = "not_recorded";

    /**
     * Instantiates a new Xml file handler with specified handler and folders for proceeded or corrupted files.
     *
     * @param handler              the handler
     * @param filePath             the file path
     * @param processedFilesFolder the processed files folder
     * @param corruptedFilesFolder the corrupted files folder
     */
    public XmlFileHandler(XmlToDBHandler handler, String filePath, String processedFilesFolder, String corruptedFilesFolder)
    {
        this.filePath = filePath;
        this.handler = handler;
        this.processedFilesFolder = processedFilesFolder;
        this.corruptedFilesFolder = corruptedFilesFolder;
    }

    @Override
    public void run()
    {
        InputStream fileInputStream = null;

        try
        {
            fileInputStream = new FileInputStream(filePath);
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(fileInputStream, handler);
            handler.writeXmlToDB();
        } catch (ParserConfigurationException | IOException e)
        {
            LOGGER.error(e);
        } catch (SAXException e)
        {
            LOGGER.error(e);
            if (corruptedFilesFolder != null)
            {
                try
                {
                    fileInputStream.close();
                } catch (IOException e1)
                {
                    LOGGER.error(e);
                }
                moveFileTo(corruptedFilesFolder);
            }
        } catch (SQLException e)
        {
            LOGGER.error(e);
            File file = new File(filePath);
            String notRecordedInBDPath = file.getParent() + File.separator + NOT_RECORDED_FILES_FOLDER;
            File notRecordedInBDFile = new File(notRecordedInBDPath);
            if (!notRecordedInBDFile.exists() && !notRecordedInBDFile.mkdirs())
            {
                LOGGER.error("The directory " + notRecordedInBDPath + " was not created.");
            }
            moveFileTo(notRecordedInBDPath);
        }

        if (processedFilesFolder != null)
        {
            moveFileTo(processedFilesFolder);
            LOGGER.info("File " + filePath + " was successfully parsed and recorded to database.");
        }
    }


    /**
     * Moves current xml file to specified folder
     *
     * @param folderName where to move
     */
    private void moveFileTo(String folderName)
    {
        Path inputFilePath = Paths.get(filePath);
        String fullDestinationPathName = folderName + File.separator + inputFilePath.getFileName();
        Path destinationPath = Paths.get(fullDestinationPathName);
        File file = new File(folderName);
        if (!file.exists() && !file.mkdirs())
        {
            LOGGER.error("The directory " + folderName + " was not created.");
        }
        try
        {
            if (Files.exists(destinationPath))
            {
                String timestamp = Instant.now().toString().replace(":", ".");
                String fullNewDestinationPath = folderName + File.separator + timestamp + "_" + inputFilePath.getFileName();
                Path newDestinationPath = Paths.get(fullNewDestinationPath);
                Files.move(inputFilePath, newDestinationPath);
            }
            else
            {
                Files.move(inputFilePath, destinationPath);
            }
        } catch (IOException | InvalidPathException e)
        {
            LOGGER.error(e);
        }
    }
}
