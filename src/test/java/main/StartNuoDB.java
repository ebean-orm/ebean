package main;

import io.ebean.docker.commands.NuoDBConfig;
import io.ebean.docker.commands.NuoDBContainer;

public class StartNuoDB {

  public static void main(String[] args) {

    NuoDBConfig config = new NuoDBConfig();
    config.setSchema("test_user");

    NuoDBContainer container = new NuoDBContainer(config);
    container.stopRemove();
    container.startWithDropCreate();
  }
}
