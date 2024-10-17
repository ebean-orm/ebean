package org.tests.transaction;

import io.ebean.DB;
import io.ebean.Transaction;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Country;
import org.tests.model.basic.Customer;
import org.tests.model.basic.OrderDetail;
import org.tests.model.basic.ResetBasicData;

import java.util.Arrays;

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

      assertThatThrownBy(() ->  DB.sqlUpdate("update o_customer set name = ? where name = ?")
        .setParameter(1, "Rob2")
        .setParameter(2, "Rob").execute())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("This transaction is read-only");

      assertThatThrownBy(() ->  DB.createUpdate(Customer.class, "update customer set name = ? where name = ?")
        .setParameter(1, "Rob2")
        .setParameter(2, "Rob").execute())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("This transaction is read-only");

      Customer customer = DB.find(Customer.class).where().eq("name", "Rob").findOne();
      customer.setName("Rob2");
      assertThatThrownBy(() ->  DB.save(customer))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("This transaction is read-only");

      txn.commit();
    }

  }

  @Test
  public void testReadOnlyTransactionWithDeleteQuery() {

    ResetBasicData.reset();

    try (Transaction txn = DB.beginTransaction()) {
      txn.setReadOnly(true);

      assertThatThrownBy(() -> DB.find(OrderDetail.class).where().eq("id", 1).delete())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("This transaction is read-only");

      assertThatThrownBy(() ->  DB.sqlUpdate("delete o_order_detail where id = ?")
        .setParameter(1, 1).execute())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("This transaction is read-only");

      assertThatThrownBy(() ->  DB.createUpdate(OrderDetail.class, "delete o_order_detail where id = ?")
        .setParameter(1, 1).execute())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("This transaction is read-only");

      OrderDetail orderDetail = DB.find(OrderDetail.class).where().eq("id", 1).findOne();
      assertThatThrownBy(() ->  DB.delete(orderDetail))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("This transaction is read-only");

      assertThatThrownBy(() ->  DB.deleteAll(Arrays.asList(orderDetail)))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("This transaction is read-only");

      txn.commit();
    }

  }

  @Test
  public void testReadOnlyTransactionWithInsertQuery() {

    ResetBasicData.reset();

    try (Transaction txn = DB.beginTransaction()) {
      txn.setReadOnly(true);

      Country country = new Country();
      country.setName("Germany");
      country.setCode("DE");

      assertThatThrownBy(() ->  DB.save(country))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("This transaction is read-only");

      assertThatThrownBy(() ->  DB.saveAll(country))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("This transaction is read-only");

      assertThatThrownBy(() ->  DB.insert(country))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("This transaction is read-only");

      assertThatThrownBy(() ->  DB.insertAll(Arrays.asList(country)))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("This transaction is read-only");

      txn.commit();
    }

  }

}
