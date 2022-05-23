package main;

import io.ebean.docker.commands.MySqlContainer;

public class StartMySql {

  public static void main(String[] args) {

    MySqlContainer.builder("8.0")
      .dbName("unit")
      .user("unit")
      .password("unit")
      .build()
      .startWithDropCreate();

    // by default this mysql docker collation is case sensitive
    // using utf8mb4_bin
    //
    // when changing to a CI collation (e.g. utf8mb4_unicode_ci) we also set
    // ebean.<db>.caseSensitiveCollation=false
    // ... such that tests now take that into account
//    config.setCollation("default");
//    config.setCollation("utf8mb4_unicode_ci");
//    config.setCharacterSet("utf8mb4");

  }
}
