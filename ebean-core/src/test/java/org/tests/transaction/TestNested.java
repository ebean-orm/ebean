package org.tests.transaction;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.junit.Test;

import javax.persistence.PersistenceException;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;

public class TestNested extends BaseTestCase {

  @Test
  public void testRunnableFail() {

    try {
      Ebean.execute(this::willFail);
    } catch (PersistenceException e) {
      assertThat(e.getMessage()).contains("test runnable rollback");
    }
  }

  @Test
  public void testCallableFail() {

    try {
      Ebean.execute(this::willFailCallable);
    } catch (PersistenceException e) {
      assertThat(e.getMessage()).contains("test callable rollback");
    }
  }

  private void willFail() {
    Ebean.executeCall(() -> {
      throw new RuntimeException("test runnable rollback");
    });
  }

  private void willFailCallable() {
    Ebean.executeCall((Callable<String>) () -> {
      throw new Exception("test callable rollback");
    });
  }
}
