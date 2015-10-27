package com.avaje.ebeaninternal.server.type;

import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.PostgresPlatform;
import com.avaje.ebeaninternal.server.core.BootupClasses;
import org.junit.Test;

import static org.junit.Assert.*;

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