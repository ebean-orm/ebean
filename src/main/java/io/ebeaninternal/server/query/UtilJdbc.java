package io.ebeaninternal.server.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UtilJdbc {

  private static final Logger logger = LoggerFactory.getLogger(UtilJdbc.class);

  public static void close(ResultSet resultSet) {
    try {
      if (resultSet != null) {
        resultSet.close();
      }
    } catch (SQLException e) {
      logger.error("Error closing ResultSet", e);
    }
  }

  public static void close(PreparedStatement statement) {
    try {
      if (statement != null) {
        statement.close();
      }
    } catch (SQLException e) {
      logger.error("Error closing PreparedStatement", e);
    }
  }

}
