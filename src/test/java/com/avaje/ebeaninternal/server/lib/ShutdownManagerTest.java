package com.avaje.ebeaninternal.server.lib;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import org.junit.Ignore;
import org.junit.Test;

public class ShutdownManagerTest extends BaseTestCase {

  /**
   * Run this test manually.
   */
  @Ignore
  @Test
  public void test_deregisterShutdownHook() {

    Ebean.getDefaultServer();
    ShutdownManager.deregisterShutdownHook();
  }

  /**
   * Run this test manually.
   */
  @Ignore
  @Test
  public void test_noShutdownHook() {

    System.setProperty("ebean.registerShutdownHook","false");
    Ebean.getDefaultServer();
  }

}