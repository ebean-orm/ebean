package main;

import io.ebean.test.containers.MariaDBContainer;

public class StartMariaDb {

  public static void main(String[] args) {
    MariaDBContainer.builder("10.6")
      .dbName("unit")
      .user("unit")
      .password("unit")
      .port(5306)
      .build()
      .start();
  }
}
