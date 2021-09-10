package main;

import io.ebean.docker.commands.MariaDBConfig;
import io.ebean.docker.commands.MariaDBContainer;

public class StartMariaDb {

  public static void main(String[] args) {

    MariaDBConfig config = new MariaDBConfig("10.5");
    config.setDbName("unit");
    config.setUser("unit");
    config.setPassword("unit");

    MariaDBContainer container = new MariaDBContainer(config);
    container.startWithDropCreate();
  }
}
