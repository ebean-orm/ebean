package com.avaje.ebeaninternal.server.lib.sql;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebeaninternal.server.core.DefaultBackgroundExecutor;
import com.avaje.tests.model.basic.Customer;

public class TestDataSourceMaxWithEntity extends BaseTestCase {

  @Test
  public void test() {

    boolean skipThisTest = true;

    if (skipThisTest) {
      return;
    }

    EbeanServer server = Ebean.getServer(null);
    

    DefaultBackgroundExecutor bg = new DefaultBackgroundExecutor(1, 2, 180, 30, "testDs");

    try {
      for (int i = 0; i < 12; i++) {
        // Thread.sleep(10*i);
        bg.execute(new ConnRunner(server, 4000, i));
      }

      System.out.println("main thread sleep ... ");

   
      Thread.sleep(30000);
      
      server.shutdown(true, false);

    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  private static class ConnRunner implements Runnable {

    final EbeanServer server;
    final long sleepMillis;
    final int position;

    ConnRunner(EbeanServer server, long sleepMillis, int position) {
      this.server = server;
      this.sleepMillis = sleepMillis;
      this.position = position;
    }

    public void run() {
      
      server.find(Customer.class).findRowCount();
      try {
        System.out.println(position+" sleep " + sleepMillis);
        Thread.sleep(sleepMillis);
        System.out.println(position+" sleep done");
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
