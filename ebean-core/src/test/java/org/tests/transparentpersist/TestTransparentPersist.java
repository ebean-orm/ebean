package org.tests.transparentpersist;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.Transaction;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeantest.LoggedSql;
import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.EBasicVer;
import org.tests.model.basic.Order;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestTransparentPersist extends BaseTestCase {

  @Test
  public void simpleInsertUpdateDelete_experimental() {

    EBasicVer b0 = new EBasicVer("simpleIUD_0");
    b0.save();
    EBasicVer b1 = new EBasicVer("simpleIUD_1");
    b1.save();
    EBasicVer b2 = new EBasicVer("simpleIUD_2");
    b2.save();

    EBasicVer newBean;
    try (Transaction transaction = DB.beginTransaction()) {
      transaction.setTransparentPersistence(true); // EXPERIMENTAL feature

      EBasicVer found = DB.find(EBasicVer.class, b0.getId());
      found.setName("auto dirty");

      // delete by id
      DB.delete(EBasicVer.class, b1.getId());
      // find and delete, note the delete is batched up to execute later
      DB.delete(DB.find(EBasicVer.class, b2.getId()));

      // insert is batched up to execute later
      newBean = new EBasicVer("simpleIUD_New1");
      DB.save(newBean);
      // can still mutate newBean before flush (but not after flush yet as new bean isn't put into Persistence context)
      newBean.setName("simpleIUD_New2");

      transaction.commit();
    }

    EBasicVer after = DB.find(EBasicVer.class, b0.getId());
    assertThat(after.getName()).isEqualTo("auto dirty");

    EBasicVer wasInserted = DB.find(EBasicVer.class, newBean.getId());
    assertThat(wasInserted.getName()).isEqualTo("simpleIUD_New2");

    assertThat(DB.find(EBasicVer.class, b1.getId())).isNull();
    assertThat(DB.find(EBasicVer.class, b2.getId())).isNull();

    DB.delete(after);
    DB.delete(wasInserted);
  }

  @Test
  public void simpleUpdate_experimental() {

    EBasicVer transPersist = new EBasicVer("simulate_simpleUpdate");
    transPersist.save();

    try (Transaction transaction = DB.beginTransaction()) {
      transaction.setTransparentPersistence(true); // EXPERIMENTAL feature

      EBasicVer found = DB.find(EBasicVer.class, transPersist.getId());
      found.setName("Persisted automatically as dirty");

      transaction.commit();
    }

    EBasicVer after = DB.find(EBasicVer.class,transPersist.getId());
    assertThat(after.getName()).isEqualTo("Persisted automatically as dirty");
    DB.delete(after);
  }

  @Test
  public void updateWithPersistCascadeInsert() {

    // setup data
    Customer c0 = new Customer();
    c0.setName("firstCust");
    Order order = new Order();
    order.setStatus(Order.Status.NEW);
    order.setCustomer(c0);
    DB.save(order);

    try (Transaction transaction = DB.beginTransaction()) {
      transaction.setTransparentPersistence(true); // EXPERIMENTAL feature

      Order foundOrder = DB.find(Order.class, order.getId());
      foundOrder.setStatus(Order.Status.APPROVED);
      // cascade persist will insert this customer (even though it isn't in the persistence context)
      Customer c1 = new Customer();
      c1.setName("newCust CascadePersist");
      foundOrder.setCustomer(c1);

      transaction.commit();
    }

    Order checkOrder = DB.find(Order.class, order.getId());

    assertThat(checkOrder.getStatus()).isEqualTo(Order.Status.APPROVED);
    assertThat(checkOrder.getCustomer().getName()).isEqualTo("newCust CascadePersist");

    DB.delete(checkOrder);
    DB.delete(Customer.class, checkOrder.getCustomer().getId());
    DB.delete(Customer.class, c0.getId());
  }

  @Test
  public void updateReferenceOnlyWithPersistCascade_Insert_andUpdateForeignKey() {

    // setup data
    Customer c0 = new Customer();
    c0.setName("firstCust");
    Order order = new Order();
    order.setStatus(Order.Status.NEW);
    order.setCustomer(c0);
    DB.save(order);

    LoggedSql.start();

    try (Transaction transaction = DB.beginTransaction()) {
      transaction.setTransparentPersistence(true); // EXPERIMENTAL feature

      Order foundOrder = DB.find(Order.class, order.getId());
      // we ONLY mutate the foreign key
      // cascade persist will insert this customer (even though it isn't in the persistence context)
      Customer c1 = new Customer();
      c1.setName("newCust CascadePersist");
      foundOrder.setCustomer(c1);

      transaction.commit();
    }

    List<String> sql = LoggedSql.stop();

    Order checkOrder = DB.find(Order.class, order.getId());

    assertThat(checkOrder.getStatus()).isEqualTo(Order.Status.NEW);
    assertThat(checkOrder.getCustomer().getName()).isEqualTo("newCust CascadePersist");

    assertThat(sql).hasSize(5);
    assertThat(sql.get(0)).contains("select t0.id, t0.status, t0.order_date");
    assertThat(sql.get(1)).contains("insert into o_customer");
    assertThat(sql.get(2)).contains(" -- bind(");
    assertThat(sql.get(3)).contains("update o_order set updtime=?, kcustomer_id=? where id=? and updtime=?");
    assertThat(sql.get(4)).contains(" -- bind(");

    DB.delete(checkOrder);
    DB.delete(Customer.class, checkOrder.getCustomer().getId());
    DB.delete(Customer.class, c0.getId());
  }

  @Test
  public void simulate_transparentPersistence_forSimpleUpdate() {

    EBasicVer transPersist = new EBasicVer("simulate_simpleUpdate");
    transPersist.save();

    try (Transaction transaction = DB.beginTransaction()) {
      transaction.setBatchMode(true);
      transaction.setBatchSize(10);

      EBasicVer found = DB.find(EBasicVer.class, transPersist.getId());
      found.setName("Changed");

      // simulate transparent persistence
      List<Object> dirtyBeans = simulateTransparentPersist(transaction);
      assertThat(dirtyBeans).hasSize(1);
      assertThat(dirtyBeans).contains(found);

      // would occur as first part of flush
      transaction.flush();
      transaction.commit();
    }

    EBasicVer after = DB.find(EBasicVer.class,transPersist.getId());
    assertThat(after.getName()).isEqualTo("Changed");
    DB.delete(after);
  }

  private List<Object> simulateTransparentPersist(Transaction transaction) {
    Database db = DB.getDefault();
    List<Object> dirtyBeans = getDirtyBeansFromPersistenceContext(transaction);
    for (Object dirtyBean : dirtyBeans) {
      db.update(dirtyBean, transaction);
    }
    return dirtyBeans;
  }

  private List<Object> getDirtyBeansFromPersistenceContext(Transaction transaction) {
    return ((SpiTransaction)transaction).getPersistenceContext().dirtyBeans();
  }

}
