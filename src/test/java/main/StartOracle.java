package main;

import io.ebean.docker.commands.OracleConfig;
import io.ebean.docker.commands.OracleContainer;

public class StartOracle {

  public static void main(String[] args) {

    OracleConfig config = new OracleConfig();
//    config.setImage("quillbuilduser/oracle-18-xe:latest");
    config.setUser("test_ebean");

    OracleContainer container = new OracleContainer(config);
    container.start();//WithDropCreate();
  }
}
