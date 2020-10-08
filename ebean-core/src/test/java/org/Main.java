package org;

import io.ebean.Ebean;
import io.ebean.EbeanServer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Main {

  public static void main(String[] args) throws SQLException {


    EbeanServer ms = Ebean.getServer("ms");
    DataSource dataSource = ms.getPluginApi().getDataSource();

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
