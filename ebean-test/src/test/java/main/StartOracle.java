package main;

import io.ebean.test.containers.OracleContainer;

public class StartOracle {

  public static void main(String[] args) {
    OracleContainer.builder("latest")
      .user("test_ebean")
      .build()
      .startWithDropCreate();
  }
}
