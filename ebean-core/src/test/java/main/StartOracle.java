package main;

import io.ebean.docker.commands.OracleConfig;
import io.ebean.docker.commands.OracleContainer;

public class StartOracle {

  public static void main(String[] args) {

    OracleConfig config = new OracleConfig();
    config.setUser("test_ebean");

    OracleContainer container = new OracleContainer(config);
    container.startWithDropCreate();
  }
}
