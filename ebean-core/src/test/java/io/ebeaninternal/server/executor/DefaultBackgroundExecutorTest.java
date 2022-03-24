package io.ebeaninternal.server.executor;

import io.ebean.config.MdcBackgroundExecutorWrapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultBackgroundExecutorTest {

  @Test
  public void submit_callable() throws Exception {

    DefaultBackgroundExecutor es = new DefaultBackgroundExecutor(1, 2, "test", null);

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
    DefaultBackgroundExecutor es = new DefaultBackgroundExecutor(1, shutdownWaitSecs, "test", null);

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
  @Disabled("test takes long time")
  public void shutdown_when_running_expect_waitAndNiceShutdown() {

    DefaultBackgroundExecutor es = new DefaultBackgroundExecutor(1, 20, "test", null);

    es.execute(new RunFor(3000, "a"));
    es.execute(new RunFor(3000, "b"));
    es.execute(new RunFor(3000, "c"));

    es.shutdown();
  }

  @Test
  @Disabled("test takes long time")
  public void shutdown_when_rougeRunnable_expect_InterruptedException() {

    DefaultBackgroundExecutor es = new DefaultBackgroundExecutor(1, 10, "test", null);

    es.execute(new RunFor(300000, "a"));
    es.execute(new RunFor(3000, "b"));
    es.execute(new RunFor(3000, "c"));

    es.shutdown();
  }

  @Test
  public void wrapWithNoMDC() {
    DefaultBackgroundExecutor es = new DefaultBackgroundExecutor(1, 10, "test", null);
    assertThat(MDC.getCopyOfContextMap()).isNull();
    es.wrap(() -> {
      assertThat(MDC.getCopyOfContextMap()).isNull();
    });
    es.wrap(() -> {
      assertThat(MDC.getCopyOfContextMap()).isNull();
      return "Callable";
    });
    es.shutdown();
  }

  @Test
  public void wrapWithMDC_expect_() throws Exception {
    DefaultBackgroundExecutor es = new DefaultBackgroundExecutor(1, 10, "test", new MdcBackgroundExecutorWrapper());
    // MDC has a copyOnThread map. So we must pass different values to check if the test will work
    MDC.clear();
    es.submit(()->{
      assertThat(MDC.get("hello")).isNull();
    }).get();

    MDC.put("hello", "there");
    es.wrap(() -> {
      assertThat(MDC.get("hello")).isEqualTo("there");
    }).run(); // will clear the MDC. But this should be OK

    MDC.put("hello", "there");
    es.wrap(() -> {
      assertThat(MDC.get("hello")).isEqualTo("there");
      return "Callable";
    }).call(); // will clear the MDC. But this should be OK

    MDC.put("hello", "there");

    CountDownLatch latch = new CountDownLatch(1);
    es.execute(() -> {
      // the assertion is executed async, so it will only logged on console
      assertThat(MDC.get("hello")).isEqualTo("there");
      latch.countDown();
    });
    assertTrue(latch.await(5, TimeUnit.SECONDS));

    es.submit(() -> {
      assertThat(MDC.get("hello")).isEqualTo("there");
      return "Callable";
    }).get();
    MDC.clear();

    es.execute(()->{
      assertThat(MDC.get("hello")).isNull();
    });
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
