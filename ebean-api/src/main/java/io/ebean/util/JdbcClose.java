package io.ebean.util;

import io.ebean.EbeanVersion;

import java.lang.System.Logger.Level;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility for closing raw Jdbc resources.
 */
public final class JdbcClose {

  private static final System.Logger log = EbeanVersion.log;

  /**
   * Close the resultSet logging if an error occurs.
   */
  public static void close(Statement statement) {
    try {
      if (statement != null) {
        statement.close();
      }
    } catch (SQLException e) {
      log.log(Level.WARNING, "Error closing statement", e);
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
      log.log(Level.WARNING, "Error closing resultSet", e);
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
      log.log(Level.WARNING, "Error closing connection", e);
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
      log.log(Level.WARNING, "Error on connection rollback", e);
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
      log.log(Level.WARNING, "Error on cancelling statement", e);
    }
  }
}
