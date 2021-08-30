package io.ebeaninternal.server.lib;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.event.ShutdownManager;
import org.junit.Ignore;
import org.junit.Test;

public class ShutdownManagerTest extends BaseTestCase {

  /**
   * Run this test manually. Most typical when we want the application code to control shutdown.
   */
  @Ignore
  @Test
  public void test_disableShutdownHook_shutdownManually() {
    // disable hook to make sure app code controls when shutdown is executed
    ShutdownManager.deregisterShutdownHook();
    DB.getDefault();

    System.out.println("shutdown manually ... ");
    // application code explicitly calls shutdown()
    ShutdownManager.shutdown();
  }

  /**
   * Run this test manually.
   */
  @Ignore
  @Test
  public void test_shutdownHook() {
    DB.getDefault(); // shutdown fired via shutdown hook, default behaviour
  }

  /**
   * Run this test manually.
   */
  @Ignore
  @Test
  public void test_disableShutdownHook() {
    DB.getDefault();
    ShutdownManager.deregisterShutdownHook(); // no shutdown is run here (not great, don't do this)
  }

  /**
   * Run this test manually.
   */
  @Ignore
  @Test
  public void test_shutdownManually() {
    DB.getDefault();
    System.out.println("shutdown manually ... ");
    // note this removes the shutdown hook, only "useful" if it runs BEFORE a JVM shutdown is invoked (hook invoked)
    DatabaseFactory.shutdown();
  }

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
