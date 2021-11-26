package main;

import io.ebean.docker.commands.Db2Config;
import io.ebean.docker.commands.Db2Container;

public class StartDb2 {

  public static void main(String[] args) {

    Db2Config config = new Db2Config("11.5.6.0a");
    config.setDbName("unit");
    config.setUser("unit");
    config.setPassword("unit");

    // to change collation, charset and other parameters like pagesize:
    config.setCreateOptions("USING CODESET UTF-8 TERRITORY DE COLLATE USING IDENTITY PAGESIZE 32768");
    config.setConfigOptions("USING STRING_UNITS CODEUNITS32");

    Db2Container container = new Db2Container(config);
    container.startWithDropCreate();
  }
}
