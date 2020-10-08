package io.ebeaninternal.server.core;

import org.junit.Ignore;
import org.junit.Test;

public class DefaultBackgroundExecutorTest {

  @Test
  @Ignore("test takes long time")
  public void shutdown_when_running_expect_waitAndNiceShutdown() throws Exception {

    DefaultBackgroundExecutor es = new DefaultBackgroundExecutor(1, 20, "test");

    es.execute(new RunFor(3000, "a"));
    es.execute(new RunFor(3000, "b"));
    es.execute(new RunFor(3000, "c"));

    es.shutdown();
  }

  @Test
  @Ignore("test takes long time")
  public void shutdown_when_rougeRunnable_expect_InterruptedException() throws Exception {

    DefaultBackgroundExecutor es = new DefaultBackgroundExecutor(1, 10, "test");

    es.execute(new RunFor(300000, "a"));
    es.execute(new RunFor(3000, "b"));
    es.execute(new RunFor(3000, "c"));

    es.shutdown();
  }


  class RunFor implements Runnable {

    final long wait;
    final String id;

    RunFor(long wait, String id) {
      this.wait = wait;
      this.id = id;
    }

    @Override
    public void run() {
      try {
        System.out.println("start " + id);
        Thread.sleep(wait);
        System.out.println("done " + id);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
