package main;

import io.ebean.docker.commands.SqlServerConfig;
import io.ebean.docker.commands.SqlServerContainer;

public class StartSqlServer {

  public static void main(String[] args) {

    SqlServerConfig config = new SqlServerConfig("2019-GA-ubuntu-16.04");
    config.setDbName("test_ebean");
    config.setUser("test_ebean");

    // by default this sqlserver docker collation is case sensitive
    // using MSSQL_COLLATION=Latin1_General_100_BIN2
    //
    // when changing to a CI collation also use
    // ebean.sqlserver.caseSensitiveCollation=false
    // ... such that tests now take that into account

    //config.setCollation("default");
    //config.setCollation("Latin1_General_100_CI");

    SqlServerContainer container = new SqlServerContainer(config);
    container.start();
  }
}
