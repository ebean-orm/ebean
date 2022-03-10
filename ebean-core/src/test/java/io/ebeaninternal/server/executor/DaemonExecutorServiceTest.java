package io.ebeaninternal.server.executor;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

class DaemonExecutorServiceTest {

  private final int count = 10;
  private final int waitMillis = 100;

  @Test
  void submit() throws Exception {
    DaemonExecutorService des = new DaemonExecutorService(5, "junk");
    long start = System.currentTimeMillis();
    List<Future<?>> futures = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      futures.add(des.submit(this::doStuff));
    }
    for (Future<?> f: futures) {
      f.get();
    }
    long exeMillis = System.currentTimeMillis() - start;
    assertThat(exeMillis).isLessThan(count * waitMillis);
    des.shutdown();
  }

  @Test
  void submit_via_DefaultBackgroundExecutor() throws Exception {
    DefaultBackgroundExecutor des = new DefaultBackgroundExecutor(1, 5, "junk", null);
    long start = System.currentTimeMillis();
    List<Future<?>> futures = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      futures.add(des.submit(this::doStuff));
    }
    for (Future<?> f: futures) {
      f.get();
    }
    long exeMillis = System.currentTimeMillis() - start;
    assertThat(exeMillis).isLessThan(count * waitMillis);
    des.shutdown();
  }

  private void doStuff() {
    try {
      Thread.sleep(waitMillis);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

}
