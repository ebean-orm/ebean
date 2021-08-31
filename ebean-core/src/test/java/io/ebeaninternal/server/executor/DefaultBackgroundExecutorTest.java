package io.ebeaninternal.server.executor;

import io.ebeaninternal.server.executor.DefaultBackgroundExecutor;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.MDC;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultBackgroundExecutorTest {

  @Test
  public void submit_callable() throws Exception {

    DefaultBackgroundExecutor es = new DefaultBackgroundExecutor(1, 2, "test");

    final Future<String> future0 = es.submit(() -> "Hello");
    final Future<String> future1 = es.submit(() -> "There");
    final Future<String> future2 = es.submit(() -> {
      try {
        Thread.sleep(100);
        return "Slow";
      } catch (InterruptedException e) {
        e.printStackTrace();
        return "Interrupted";
      }
    });

    es.shutdown();

    assertThat(future0.get()).isEqualTo("Hello");
    assertThat(future1.get(1, TimeUnit.SECONDS)).isEqualTo("There");
    assertThat(future2.get()).isEqualTo("Slow");
  }

  @Test
  public void shutdown_slowCallable_expect_interrupted() throws Exception {

    int shutdownWaitSecs = 1;
    DefaultBackgroundExecutor es = new DefaultBackgroundExecutor(1, shutdownWaitSecs, "test");

    final Future<String> future2 = es.submit(() -> {
      try {
        Thread.sleep(1500); // longer than shutdown wait
        return "Slow";
      } catch (InterruptedException e) {
        // expected for this test
        Thread.currentThread().interrupt();
        return "Interrupted";
      }
    });

    // shutdown waits max shutdownWaitSecs seconds for active tasks
    es.shutdown();
    assertThat(future2.get()).isEqualTo("Interrupted");
  }

  @Test
  @Ignore("test takes long time")
  public void shutdown_when_running_expect_waitAndNiceShutdown() {

    DefaultBackgroundExecutor es = new DefaultBackgroundExecutor(1, 20, "test");

    es.execute(new RunFor(3000, "a"));
    es.execute(new RunFor(3000, "b"));
    es.execute(new RunFor(3000, "c"));

    es.shutdown();
  }

  @Test
  @Ignore("test takes long time")
  public void shutdown_when_rougeRunnable_expect_InterruptedException() {

    DefaultBackgroundExecutor es = new DefaultBackgroundExecutor(1, 10, "test");

    es.execute(new RunFor(300000, "a"));
    es.execute(new RunFor(3000, "b"));
    es.execute(new RunFor(3000, "c"));

    es.shutdown();
  }

  @Test
  public void wrapWithNoMDC() {
    DefaultBackgroundExecutor es = new DefaultBackgroundExecutor(1, 10, "test");
    assertThat(MDC.getCopyOfContextMap()).isNull();
    es.wrapMDC(() -> {
      assertThat(MDC.getCopyOfContextMap()).isNull();
    });
    es.wrapMDC(() -> {
      assertThat(MDC.getCopyOfContextMap()).isNull();
      return "Callable";
    });
    es.shutdown();
  }

  @Test
  public void wrapWithMDC_expect_() {
    DefaultBackgroundExecutor es = new DefaultBackgroundExecutor(1, 10, "test");
    MDC.clear();
    MDC.put("hello", "there");
    es.wrapMDC(() -> {
      assertThat(MDC.get("hello")).isEqualTo("there");
    });
    es.wrapMDC(() -> {
      assertThat(MDC.get("hello")).isEqualTo("there");
      return "Callable";
    });
    es.execute(() -> {
      assertThat(MDC.get("hello")).isEqualTo("there");
    });
    es.submit(() -> {
      assertThat(MDC.get("hello")).isEqualTo("there");
      return "Callable";
    });
    MDC.clear();
    es.shutdown();
  }

  private static class RunFor implements Runnable {

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
