package io.ebean.xtest.base;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.ebean.DatabaseFactory;

public class ServerStartTest {

  @Test
  @Disabled("run manually")
  void testServerStartAndMigrateDb2() throws Exception {
    DatabaseFactory.create("db2-migration").shutdown();
  }
}
