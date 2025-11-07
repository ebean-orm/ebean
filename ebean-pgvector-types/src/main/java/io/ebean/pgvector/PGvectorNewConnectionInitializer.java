package io.ebean.pgvector;

import com.pgvector.PGvector;
import io.ebean.datasource.NewConnectionInitializer;

import java.sql.Connection;
import java.sql.SQLException;

public class PGvectorNewConnectionInitializer implements NewConnectionInitializer {

  @Override
  public void preInitialize(Connection connection) {
    try {
      PGvector.registerTypes(connection);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
