package main;

import io.ebean.docker.commands.PostgresConfig;
import io.ebean.docker.commands.PostgresContainer;

public class StartPostgres {

  public static void main(String[] args) {

    PostgresConfig config = new PostgresConfig("13");
    config.setPort(5432);
    config.setDbName("unit");
    config.setUser("unit");
    config.setPassword("unit");
    config.setContainerName("pg13x");
    config.setExtensions("hstore,pgcrypto");

    PostgresContainer container = new PostgresContainer(config);
    container.startWithDropCreate();
  }
}
