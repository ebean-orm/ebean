package main;

import io.ebean.docker.commands.PostgresContainer;

public class StartPostgres {

  public static void main(String[] args) {
    PostgresContainer.builder("13")
      .port(5432)
      .dbName("unit")
      .user("unit")
      .password("unit")
      .containerName("pg13x")
      .extensions("hstore,pgcrypto")
      .build()
      .startWithDropCreate();
  }
}
