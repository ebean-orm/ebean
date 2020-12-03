package io.ebeaninternal.server.executor;

import io.ebean.BackgroundExecutor;
import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Database;
import org.junit.Ignore;
import org.junit.Test;
import org.tests.model.basic.Customer;

public class TestShutdownWithBackgroundTasks extends BaseTestCase {

  @Test
  @Ignore
  public void test() {

    Database server = DB.getDefault();
    final BackgroundExecutor bg = server.getBackgroundExecutor();
    try {
      for (int i = 0; i < 12; i++) {
        bg.execute(new Job(server, 500, i));
      }

      Thread.sleep(1000);
      server.shutdown();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static class Job implements Runnable {

    final Database server;
    final long sleepMillis;
    final int position;

    Job(Database server, long sleepMillis, int position) {
      this.server = server;
      this.sleepMillis = sleepMillis;
      this.position = position;
    }

    @Override
    public void run() {
      try {
        System.out.println(position + " sleep " + sleepMillis);
        Thread.sleep(sleepMillis);
        server.find(Customer.class).findCount();
        System.out.println(position + " sleep done");
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        e.printStackTrace();
      }
    }
  }
}
