package main;

import io.ebean.test.containers.Db2Container;

public class StartDb2 {

  public static void main(String[] args) {
    Db2Container.builder("11.5.9.0")
      .dbName("unit")
      .user("unit")
      .password("unit")
      // to change collation, charset and other parameters like pagesize:
      .createOptions("USING CODESET UTF-8 TERRITORY DE COLLATE USING IDENTITY PAGESIZE 32768")
      .configOptions("USING STRING_UNITS CODEUNITS32")
      .build()
      .startWithDropCreate();
  }
}
