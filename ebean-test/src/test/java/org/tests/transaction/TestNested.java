package org.tests.transaction;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;

import jakarta.persistence.PersistenceException;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;

class TestNested extends BaseTestCase {

  @Test
  void testRunnableFail() {
    try {
      DB.execute(this::willFail);
    } catch (PersistenceException e) {
      assertThat(e.getMessage()).contains("test runnable rollback");
    }
  }

  @Test
  void testCallableFail() {
    try {
      DB.execute(this::willFailCallable);
    } catch (PersistenceException e) {
      assertThat(e.getMessage()).contains("test callable rollback");
    }
  }

  private void willFail() {
    DB.executeCall(() -> {
      throw new RuntimeException("test runnable rollback");
    });
  }

  private void willFailCallable() {
    DB.executeCall((Callable<String>) () -> {
      throw new Exception("test callable rollback");
    });
  }
}
