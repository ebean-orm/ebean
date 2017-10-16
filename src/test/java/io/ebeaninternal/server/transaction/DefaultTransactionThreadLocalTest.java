package io.ebeaninternal.server.transaction;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebeaninternal.api.SpiTransaction;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class DefaultTransactionThreadLocalTest extends BaseTestCase {

  @Test
  public void get() throws Exception {

    if (isH2()) {
      Ebean.execute(() -> {
        SpiTransaction txn = DefaultTransactionThreadLocal.get("h2");
        assertNotNull(txn);
      });

      // thread local should be set to null
      assertNull(DefaultTransactionThreadLocal.get("h2"));
    }
  }

}
