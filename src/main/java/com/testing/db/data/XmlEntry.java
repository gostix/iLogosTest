package com.testing.db.data;

/**
 * The basic interface for implementing Xml entry with some content and creation date
 */
public interface XmlEntry
{
    /**
     * Gets content.
     *
     * @return the content
     */
    String getContent();

    /**
     * Sets content.
     *
     * @param content the content
     */
    void setContent(String content);

    /**
     * Gets creation date.
     *
     * @return the creation date
     */
    String getCreationDate();

    /**
     * Sets creation date.
     *
     * @param creationDate the creation date
     */
    void setCreationDate(String creationDate);

}
