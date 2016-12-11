package org.tests.transaction;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.TxRunnable;
import org.junit.Assert;
import org.junit.Test;

public class TestNested extends BaseTestCase {

  @Test
  public void test() {

    try {
      Ebean.execute(() -> willFail());
    } catch (RuntimeException e) {
      Assert.assertEquals(e.getMessage(), "test rollback");
    }
  }

  private void willFail() {
    Ebean.execute(new TxRunnable() {
      public void run() {

        String msg = "test rollback";
        throw new RuntimeException(msg);

      }
    });
  }
}
