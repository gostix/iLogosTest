package com.testing.handler;

import org.xml.sax.helpers.DefaultHandler;

import java.sql.SQLException;

/**
 * The abstract base class for parsing and writing to database xml file.
 */
abstract class XmlToDBHandler extends DefaultHandler
{
    /**
     * Write xml to db.
     *
     * @throws SQLException the sql exception
     */
    abstract void writeXmlToDB() throws SQLException;

    /**
     * Has all elements boolean.
     *
     * @return the boolean
     */
    abstract boolean hasAllElements();
}
