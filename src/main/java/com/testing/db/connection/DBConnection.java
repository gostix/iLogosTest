package com.testing.db.connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * That class for establishing connection (session) with a specific database.
 */
public class DBConnection
{

    private static final Logger LOGGER = LogManager.getLogger(DBConnection.class);

    private String jdbcDriverName;
    private String dbURL;
    private String userName;
    private String password;

    /**
     * Instantiates a new object for establishing database connection with specified properties.
     *
     * @param jdbcDriverName the jdbc driver name
     * @param dbURL          the db url
     * @param userName       the user name
     * @param password       the password
     */
    public DBConnection(String jdbcDriverName, String dbURL, String userName, String password) throws SQLException
    {
        this.jdbcDriverName = jdbcDriverName;
        this.dbURL = dbURL;
        this.userName = userName;
        this.password = password;
    }

    /**
     * Returns the connection to database.
     *
     * @return the {@link java.sql.Connection} object
     * @throws SQLException that provides information on a database access
     * error or other errors.
     */
    public Connection getConnection() throws SQLException
    {
        Connection  connection = DriverManager.getConnection(dbURL, userName, password);

        try
        {
            Class.forName(jdbcDriverName);
        } catch (ClassNotFoundException e)
        {
            throw new SQLException(jdbcDriverName + " driver was not found. " + "Include in your library path!", e);
        }
        LOGGER.trace(jdbcDriverName + " driver was Registered!");

        if (connection != null)
        {
            LOGGER.trace("Connection created");
        }
        else
        {
            throw new SQLException("Failed to make connection! Please check configuration parameters and DB availability.");
        }
        return connection;
    }

}
