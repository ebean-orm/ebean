package io.ebeaninternal.server.lib;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.event.ShutdownManager;
import org.junit.Ignore;
import org.junit.Test;

public class ShutdownManagerTest extends BaseTestCase {

  /**
   * Run this test manually.
   */
  @Ignore
  @Test
  public void test_deregisterShutdownHook() {
    DB.getDefault();
    ShutdownManager.deregisterShutdownHook();
  }

  /**
   * Run this test manually.
   */
  @Ignore
  @Test
  public void test_noShutdownHook() {
    System.setProperty("ebean.registerShutdownHook", "false");
    DB.getDefault();
  }

}
