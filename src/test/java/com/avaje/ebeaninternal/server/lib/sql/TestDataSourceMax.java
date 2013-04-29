package com.avaje.ebeaninternal.server.lib.sql;

import java.sql.Connection;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebeaninternal.server.core.DefaultBackgroundExecutor;
import com.avaje.ebeaninternal.server.lib.sql.DataSourcePool.Status;

public class TestDataSourceMax extends BaseTestCase {

  @Test
  public void test() {

    boolean runThisManuallyNow = true;

    if (!runThisManuallyNow) {
      return;
    }

    String name = "h2";

    DataSourceConfig dsConfig = new DataSourceConfig();
    dsConfig.loadSettings(name);
    dsConfig.setMinConnections(3);
    dsConfig.setMaxConnections(3);
    dsConfig.setWaitTimeoutMillis(30000);

    DataSourcePool pool = new DataSourcePool(null, name, dsConfig);

    Assert.assertEquals(3, pool.getMaxSize());

    DefaultBackgroundExecutor bg = new DefaultBackgroundExecutor(10, 2, 180, 30, "testDs");

    try {
      for (int i = 0; i < 12; i++) {
        // Thread.sleep(10*i);
        bg.execute(new ConnRunner(pool, 100));
      }

      System.out.println("main thread sleep ... " + pool.getStatus(false));

      Thread.sleep(1000);
      Status status = pool.getStatus(false);
      System.out.println(status);

      // this dumpOrder was for 3 vectors used in PooledConnectionQueue
      // that logged the order of wait, notify and obtain events
      // I have remove that code.

      // String s = pool.dumpOrder();
      // System.err.println(s);

    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  private static class ConnRunner implements Runnable {

    final DataSourcePool pool;
    final long sleepMillis;

    ConnRunner(DataSourcePool pool, long sleepMillis) {
      this.pool = pool;
      this.sleepMillis = sleepMillis;
    }

    public void run() {
      try {
        Connection connection = pool.getConnection();
        Thread.sleep(sleepMillis);
        connection.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

  }
}
