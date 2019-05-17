package main;

import io.ebean.docker.commands.MySqlConfig;
import io.ebean.docker.commands.MySqlContainer;

public class StartMySql {

  public static void main(String[] args) {

    MySqlConfig config = new MySqlConfig("5.7");
    config.setDbName("unit");
    config.setUser("unit");
    config.setPassword("unit");

    // by default this mysql docker collation is case sensitive
    // using utf8mb4_bin
    //
    // when changing to a CI collation (e.g. utf8mb4_unicode_ci) we also set
    // ebean.<db>.caseSensitiveCollation=false
    // ... such that tests now take that into account
//    config.setCollation("default");
//    config.setCollation("utf8mb4_unicode_ci");
//    config.setCharacterSet("utf8mb4");

    MySqlContainer container = new MySqlContainer(config);
    container.start();
  }
}
