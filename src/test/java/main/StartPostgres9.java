package main;

import io.ebean.docker.commands.PostgresConfig;
import io.ebean.docker.commands.PostgresContainer;

public class StartPostgres9 {

  public static void main(String[] args) {

    PostgresConfig config = new PostgresConfig("9.6");
    config.setPort(9432);
    config.setDbName("unit");
    config.setUser("unit");
    config.setPassword("unit");
    config.setContainerName("pg9");
    config.setExtensions("hstore,pgcrypto");

    PostgresContainer container = new PostgresContainer(config);
    container.startWithDropCreate();
  }
}
