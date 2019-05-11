package main;

import io.ebean.docker.commands.SqlServerConfig;
import io.ebean.docker.commands.SqlServerContainer;

public class StartSqlServer {

  public static void main(String[] args) {

    SqlServerConfig config = new SqlServerConfig("2017-CU4");
    config.setDbName("test_ebean");
    config.setUser("test_ebean");


    SqlServerContainer container = new SqlServerContainer(config);
    container.start();

  }
}
