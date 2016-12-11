package io.ebeaninternal.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Utility for closing raw Jdbc resources.
 */
public class JdbcClose {

  private static final Logger logger = LoggerFactory.getLogger(JdbcClose.class);

  /**
   * Close the connection logging if an error occurs.
   */
  public static void close(Connection connection) {
    try {
      if (connection != null) {
        connection.close();
      }
    } catch (SQLException e) {
      logger.warn("Error closing connection", e);
    }
  }

  /**
   * Rollback the connection logging if an error occurs.
   */
  public static void rollback(Connection connection) {
    try {
      if (connection != null) {
        connection.rollback();
      }
    } catch (SQLException e) {
      logger.warn("Error on connection rollback", e);
    }
  }
}
