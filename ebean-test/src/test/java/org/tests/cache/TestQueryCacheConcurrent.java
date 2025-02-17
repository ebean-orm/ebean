package org.tests.cache;

import io.ebean.DB;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.cache.EColAB;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestQueryCacheConcurrent extends BaseTestCase {

  private volatile boolean running;
  private volatile boolean failed;
  private volatile int  step;

  @Test
  public void testConcurrent() throws InterruptedException {

    Thread t1 = new Thread(() -> {
      while (running) {
        while (step <1) {
          if (!running) return;
        }
        System.out.println("T1 BEFORE Insert");
        new EColAB("01", "20").save();
        System.out.println("T1 AFTER Insert");
        step = 2;
        while (step < 3) {
          if (!running) return;
        }
        System.out.println("-------------------------");
        step = 1;
      }
    });

    Thread t2 = new Thread(() -> {
      while (running) {
        while (step < 1) {
          if (!running) return;
        }
        System.out.println("T2 before FIND1");
        List<EColAB> list = DB.find(EColAB.class)
          .setUseQueryCache(true)
          .where()
          .eq("columnA", "01")
          .eq("columnB", "20")
          .findList();
        System.out.println("T2 FIND1: " + list.size());
        while (step < 2) {
          if (!running) return;
        }
        System.out.println("T2 before FIND2");
        list = DB.find(EColAB.class)
          .setUseQueryCache(true)
          .where()
          .eq("columnA", "01")
          .eq("columnB", "20")
          .findList();
        System.out.println("T2 FIND2: " + list.size());
        if (list.isEmpty()) {
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
          list = DB.find(EColAB.class)
            .setUseQueryCache(true)
            .where()
            .eq("columnA", "01")
            .eq("columnB", "20")
            .findList();
          System.out.println("After 1s: in cache" + list);
          System.out.println("...          in DB" + DB.find(EColAB.class).findOne());
        }
        if (list.isEmpty()) {
          failed = true;
        }
        DB.find(EColAB.class).delete();
        step = 3;
      }
    });

    running = true;
    t1.start();
    t2.start();
    step = 1;
    Thread.sleep(1000);
    running = false;
    t1.join();
    t2.join();
    assertThat(failed).isFalse();
  }

}
