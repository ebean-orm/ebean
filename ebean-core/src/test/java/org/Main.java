package org;

import io.ebean.DB;
import io.ebean.Database;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Main {

  public static void main(String[] args) throws SQLException {

    Database ms = DB.byName("ms");
    DataSource dataSource = ms.dataSource();

    Connection connection = dataSource.getConnection();
    PreparedStatement statement = connection.prepareStatement("select next VALUE for j2_seq");
    ResultSet resultSet = statement.executeQuery();
    if (resultSet.next()) {
      Object val = resultSet.getObject(1);
      System.out.println(""+val);
    }
    connection.close();
  }
}
