package com.testing.db.data;


/**
 * The custom xml entry implementation
 */
public class CustomXmlEntry implements XmlEntry
{

    private String content;
    private String creationDate;

    @Override
    public String getContent()
    {
        return content;
    }

    @Override
    public void setContent(String content)
    {
        this.content = content;
    }

    @Override
    public String getCreationDate()
    {
        return creationDate;
    }

    @Override
    public void setCreationDate(String creationDate)
    {
        this.creationDate = creationDate;
    }

    @Override
    public String toString()
    {
        return "CustomXmlEntry{" + ", content=" + content + ", creationDate=" + creationDate + '}';
    }
}
