package com.testing.db;

import com.testing.config.Configuration;
import com.testing.db.connection.ConnectionPool;
import com.testing.db.data.XmlEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * The singleton class that writes XmlEntries to database using connection pool
 */
public class DBWriter
{
    private static final Logger LOGGER = LogManager.getLogger(DBWriter.class);

    private static final DBWriter instance = new DBWriter();
    private ConnectionPool connectionPool;

    private DBWriter()
    {
        connectionPool = new ConnectionPool(Runtime.getRuntime().availableProcessors() + 1);
    }

    /**
     * Write XmlEntry to database.
     *
     * @param xmlEntry that represents some data and creation date
     * @return the true if recorded successfully
     * @throws SQLException that provides information on a database access
     * error or other errors.
     */
    public boolean writeEntry(XmlEntry xmlEntry) throws SQLException
    {
        PreparedStatement stmt = null;
        Connection conn = connectionPool.getConnectionFromPool();

        String insertTableSQL = "INSERT INTO " + Configuration.getDBTable() + "(content, creation_date) VALUES" + "(?,?)";

        try
        {
            stmt = conn.prepareStatement(insertTableSQL);
            stmt.setString(1, xmlEntry.getContent());
            stmt.setString(2, xmlEntry.getCreationDate());
            stmt.executeUpdate();
        } catch (SQLException e)
        {
            LOGGER.error(e);
            return false;
        } finally
        {
            if (stmt != null)
            {
                try
                {
                    stmt.close();
                } catch (SQLException e)
                {
                    LOGGER.error(e);
                }
            }
        }
        LOGGER.debug("Entry was successfully written to database");
        connectionPool.returnConnectionToPool(conn);
        return true;
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static DBWriter getInstance()
    {
        return instance;
    }
}
