package org.tests.transaction;

import io.ebean.DB;
import io.ebean.Transaction;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TestTransactionReadOnly extends BaseTestCase {

  @Test
  public void testReadOnlyTransactionWithUpdateQuery() {

    ResetBasicData.reset();

    try (Transaction txn = DB.beginTransaction()) {
      txn.setReadOnly(true);

      assertThatThrownBy(() -> DB.update(Customer.class).set("name", "Rob2").where().eq("name", "Rob").update())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("This transaction is read-only");

      txn.commit();
    }

  }

}
