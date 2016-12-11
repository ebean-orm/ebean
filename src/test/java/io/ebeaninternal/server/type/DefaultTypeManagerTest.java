package io.ebeaninternal.server.type;

import io.ebean.config.ServerConfig;
import io.ebean.config.dbplatform.postgres.PostgresPlatform;
import io.ebeaninternal.server.core.bootup.BootupClasses;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DefaultTypeManagerTest {

  DefaultTypeManager typeManager;

  public DefaultTypeManagerTest() {
    ServerConfig serverConfig = new ServerConfig();
    serverConfig.setDatabasePlatform(new PostgresPlatform());
    BootupClasses bootupClasses = new BootupClasses();
    typeManager = new DefaultTypeManager(serverConfig, bootupClasses);
  }

  @Test
  public void isIntegerType() {

    assertTrue(typeManager.isIntegerType("1"));
    assertTrue(typeManager.isIntegerType("0"));

    assertFalse(typeManager.isIntegerType("A"));
    assertFalse(typeManager.isIntegerType("01"));
    assertFalse(typeManager.isIntegerType(" 01"));
    assertFalse(typeManager.isIntegerType(" 0"));
    assertFalse(typeManager.isIntegerType(" 1"));
    assertFalse(typeManager.isIntegerType(" A"));
  }

}
