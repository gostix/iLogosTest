package com.testing.handler;

import com.testing.db.data.CustomXmlEntry;
import com.testing.db.DBWriter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.sql.SQLException;

/**
 * The custom xml handler
 */
public class CustomXmlToDBHandler extends XmlToDBHandler
{
    private boolean content;
    private boolean creationDate;
    private boolean rootEntry;
    private DBWriter dbWriter = DBWriter.getInstance();
    private CustomXmlEntry xmlEntry = new CustomXmlEntry();

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        if (qName.equalsIgnoreCase("Entry"))
        {
            rootEntry = true;
        }
        else if (qName.equalsIgnoreCase("content"))
        {
            content = true;
        }
        else if (qName.equalsIgnoreCase("creationDate"))
        {
            creationDate = true;
        }
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException
    {
        if (content)
        {
            if (length > 1024)
            {
                throw new SAXException("Content length cant be more than 1024 symbols. File will replace to corrupted folder.");
            }
            xmlEntry.setContent(new String(ch, start, length));
        }
        else if (creationDate)
        {
            xmlEntry.setCreationDate(new String(ch, start, length));
        }
    }

    @Override
    boolean hasAllElements()
    {
        return rootEntry && content && creationDate;
    }

    @Override
    void writeXmlToDB() throws SQLException
    {
        if (hasAllElements())
        {
            dbWriter.writeEntry(xmlEntry);
        }
    }
}
