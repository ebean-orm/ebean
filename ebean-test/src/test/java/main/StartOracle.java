package main;

import io.ebean.docker.commands.OracleContainer;

public class StartOracle {

  public static void main(String[] args) {
    OracleContainer.newBuilder("latest")
      .user("test_ebean")
      .build()
      .startWithDropCreate();
  }
}
