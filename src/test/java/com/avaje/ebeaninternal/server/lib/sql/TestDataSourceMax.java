package com.avaje.ebeaninternal.server.lib.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebeaninternal.server.core.DefaultBackgroundExecutor;
import com.avaje.ebeaninternal.server.lib.sql.DataSourcePool.Status;

public class TestDataSourceMax extends BaseTestCase {

  @Test
  public void test() {

    boolean skipThisTest = true;

    if (skipThisTest) {
      return;
    }

    Ebean.getServer(null);
    
    String name = "h2";

    DataSourceConfig dsConfig = new DataSourceConfig();
    dsConfig.loadSettings(name);
    dsConfig.setMinConnections(2);
    dsConfig.setMaxConnections(8);
    dsConfig.setWaitTimeoutMillis(30000);
    dsConfig.setCaptureStackTrace(true);

    DataSourcePool pool = new DataSourcePool(null, name, dsConfig);

    // Assert.assertEquals(3, pool.getMaxSize());

    DefaultBackgroundExecutor bg = new DefaultBackgroundExecutor(10, 2, 180, 30, "testDs");

    try {
      for (int i = 0; i < 12; i++) {
        // Thread.sleep(10*i);
        bg.execute(new ConnRunner(pool, 4000));
      }

      System.out.println("main thread sleep ... " + pool.getStatus(false));

      Thread.sleep(10000);
      Status status = pool.getStatus(false);
      System.out.println(status);

      pool.shutdown(false);
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
      Connection connection = null;
      PreparedStatement pstmt = null;
      ResultSet rset = null;
      try {
        connection = pool.getConnection();
        pstmt = connection.prepareStatement("select count(*) from o_customer");
        rset = pstmt.executeQuery();

        System.out.println("sleep " + sleepMillis);
        Thread.sleep(sleepMillis);
        System.out.println("sleep done");

      } catch (Exception ex) {
        ex.printStackTrace();
      } finally {
        if (rset != null) {
          try {
            rset.close();
          } catch (Exception e) {
            e.printStackTrace();
          }
          if (pstmt != null) {
            try {
              pstmt.close();
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
          if (connection != null) {
            try {
              connection.close();
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        }
      }
    }
  }
}
