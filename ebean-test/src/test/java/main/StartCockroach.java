package main;

import io.ebean.docker.commands.CockroachConfig;
import io.ebean.docker.commands.CockroachContainer;

public class StartCockroach {

  public static void main(String[] args) {

    CockroachConfig config = new CockroachConfig("v21.2.4");
    config.setDbName("unit");

    CockroachContainer container = new CockroachContainer(config);
    container.start();
  }
}
