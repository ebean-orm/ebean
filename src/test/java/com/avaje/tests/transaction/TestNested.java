package com.avaje.tests.transaction;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.TxRunnable;

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
