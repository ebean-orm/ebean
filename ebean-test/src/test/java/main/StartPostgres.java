package main;

import io.ebean.test.containers.PostgresContainer;

public class StartPostgres {

  public static void main(String[] args) {
    PostgresContainer.builder("17")
      .dbName("unit")
      //.port(6432)
      //.user("unit")
      //.password("test")
      //.containerName("ut_postgres")
      .extensions("hstore,pgcrypto")
      .build()
      .startWithDropCreate();
  }
}
