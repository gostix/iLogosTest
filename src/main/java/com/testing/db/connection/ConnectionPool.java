package com.testing.db.connection;

import com.testing.config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This is connection pool class that contains specified number of connections to database
 */
public class ConnectionPool
{
    private static final Logger LOGGER = LogManager.getLogger(ConnectionPool.class);

    private final String DB_URL;
    private final String USER_NAME;
    private final String PASSWORD;
    private final String DB_DRIVER;
    private final int POOL_SIZE;
    private final BlockingQueue<Connection> connectionPool;
    private AtomicBoolean isCleared;

    /**
     * Instantiates a new database connection pool. Using {@link com.testing.config.Configuration} properties to
     * establish DB connection.
     */
    public ConnectionPool(int size)
    {
        this.POOL_SIZE = size;
        this.DB_DRIVER = Configuration.getJDBCDriverName();
        this.DB_URL = Configuration.getDBurl();
        this.USER_NAME = Configuration.getDBUserName();
        this.PASSWORD = Configuration.getPassword();
        connectionPool = new ArrayBlockingQueue<>(POOL_SIZE);
        isCleared = new AtomicBoolean(true);
    }

    /**
     * Init connection pool.
     *
     * @throws SQLException the sql exception on a database access
     * error or other errors.
     */
    private void initConnectionPool() throws SQLException
    {
        while (!checkIfConnectionPoolIsFull())
        {
            createNewConnection();
        }
        isCleared.compareAndSet(true, false);
    }


    /**
     * Check if the connection pool is full
     *
     * @return true if pool size equals maximum pool size
     */
    private synchronized boolean checkIfConnectionPoolIsFull()
    {
        return connectionPool.size() >= POOL_SIZE;
    }


    /**
     * Creates new connection to database and add it to connection pool
     *
     * @throws SQLException the sql exception on a database access
     * error or other errors.
     */
    private void createNewConnection() throws SQLException
    {
        Connection connection = new DBConnection(DB_DRIVER, DB_URL, USER_NAME, PASSWORD).getConnection();
        connectionPool.add(connection);
    }

    /**
     * Gets connection from pool.
     *
     * @return the connection from pool
     * @throws SQLException the sql exception on a database access
     * error or other errors.
     */
    public Connection getConnectionFromPool() throws SQLException
    {
        if (isCleared.get())
        {
            initConnectionPool();
        }
        try
        {
            return connectionPool.take();
        } catch (InterruptedException e)
        {
            throw new SQLException("Cannot get connection from connection pool.");
        }
    }

    /**
     * Return connection to pool if not needed anymore
     *
     * @param connection the connection to database
     * @throws SQLException connection parameter is null and cannot create new one connection instead last one
     */
    public void returnConnectionToPool(Connection connection) throws SQLException
    {
        if (connection != null)
        {
            connectionPool.add(connection);
        }
        else
        {
            createNewConnection();
        }

        if (connectionPool.remainingCapacity() == 0)
        {
            new Thread(() ->
            {
                try
                {
                    Thread.sleep(5000);
                } catch (InterruptedException e)
                {
                    LOGGER.error(e);
                }
                clearIfNoConnectionsUses();
            }).start();
        }
    }

    /**
     * Close and clear connection pool if no one uses by timeout
     */
    private synchronized void clearIfNoConnectionsUses()
    {
        if (!isCleared.get() && connectionPool.remainingCapacity() == 0)
        {
            LOGGER.debug("Close all unused connections to DB by timeout");
            for (Connection con : connectionPool)
            {
                try
                {
                    if (!con.isClosed())
                    {
                        con.close();
                    }
                } catch (SQLException e)
                {
                    LOGGER.error(e);
                }
            }
            connectionPool.clear();
            isCleared.compareAndSet(false, true);
        }
    }
}
