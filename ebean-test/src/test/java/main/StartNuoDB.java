package main;

import io.ebean.docker.commands.NuoDBContainer;

public class StartNuoDB {

  public static void main(String[] args) {
    NuoDBContainer container = NuoDBContainer.builder("4.0")
      .schema("test_user")
      .build();

    container.stopRemove();
    container.startWithDropCreate();
  }
}
