package io.ebean.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility for closing raw Jdbc resources.
 */
public class JdbcClose {

  private static final Logger log = LoggerFactory.getLogger("io.ebean");

  /**
   * Close the resultSet logging if an error occurs.
   */
  public static void close(Statement statement) {
    try {
      if (statement != null) {
        statement.close();
      }
    } catch (SQLException e) {
      log.warn("Error closing statement", e);
    }
  }

  /**
   * Close the resultSet logging if an error occurs.
   */
  public static void close(ResultSet resultSet) {
    try {
      if (resultSet != null) {
        resultSet.close();
      }
    } catch (SQLException e) {
      log.warn("Error closing resultSet", e);
    }
  }

  /**
   * Close the connection logging if an error occurs.
   */
  public static void close(Connection connection) {
    try {
      if (connection != null) {
        connection.close();
      }
    } catch (SQLException e) {
      log.warn("Error closing connection", e);
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
      log.warn("Error on connection rollback", e);
    }
  }

  /**
   * Cancels the statement
   */
  public static void cancel(Statement stmt) {
    try {
      if (stmt != null) {
        stmt.cancel();
      }
    } catch (SQLException e) {
      log.warn("Error on cancelling statement", e);
    }
  }
}
