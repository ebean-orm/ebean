package main;

import io.ebean.docker.commands.SqlServerContainer;

public class StartSqlServer {

  public static void main(String[] args) {
    SqlServerContainer.newBuilder("2019-GA-ubuntu-16.04")
      .dbName("test_ebean")
      .user("test_ebean")
      .build()
      .start();

    // by default this sqlserver docker collation is case sensitive
    // using MSSQL_COLLATION=Latin1_General_100_BIN2
    //
    // when changing to a CI collation also use
    // ebean.sqlserver.caseSensitiveCollation=false
    // ... such that tests now take that into account

    //config.setCollation("default");
    //config.setCollation("Latin1_General_100_CI");
  }
}
