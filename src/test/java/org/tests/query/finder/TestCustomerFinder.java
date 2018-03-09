package org.tests.query.finder;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Transaction;
import io.ebean.meta.BasicMetricVisitor;
import io.ebean.meta.MetaOrmQueryMetric;
import io.ebean.meta.MetaTimedMetric;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.EBasic;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class TestCustomerFinder extends BaseTestCase {

  @Test
  public void test_ref() {

    ResetBasicData.reset();

    Customer customer = Customer.find.ref(1);
    assertThat(customer.getId()).isEqualTo(1);
  }

  @Test
  public void test_all_byId_byName() {

    ResetBasicData.reset();

    List<Customer> all = Customer.find.all();
    List<Customer> list = Ebean.find(Customer.class).findList();

    assertThat(all.size()).isEqualTo(list.size());

    Customer customer = all.get(0);

    Customer customer1 = Customer.find.byId(customer.getId());

    assertThat(customer.getId()).isEqualTo(customer1.getId());
    assertThat(customer.getName()).isEqualTo(customer1.getName());

    assertThat(Customer.find.db().getName()).isEqualTo(Ebean.getDefaultServer().getName());

  }

  @Test
  public void currentTransaction() {

    Ebean.beginTransaction();
    try {
      Transaction t1 = Ebean.currentTransaction();
      Transaction t2 = Customer.find.currentTransaction();
      assertThat(t2).isSameAs(t1);

    } finally {
      Ebean.endTransaction();
    }
  }

  @Test
  public void flush() {

    Transaction transaction = Ebean.beginTransaction();
    try {
      Customer.find.currentTransaction().setBatchMode(true);

      LoggedSqlCollector.start();
      EBasic b = new EBasic("junk");
      Ebean.save(b);

      assertTrue(LoggedSqlCollector.current().isEmpty());
      Customer.find.flush();

      List<String> sql = LoggedSqlCollector.stop();
      assertThat(sql.get(0)).contains("insert into e_basic");
    } finally {
      transaction.end();
    }
  }

  @Test
  public void test_byName_deleteById() {

    Customer customer = new Customer();
    customer.setName("Newbie-879879897");

    Ebean.save(customer);
    assertThat(customer.getId()).isNotNull();

    Customer customer2 = Customer.find.byName(customer.getName());
    assertThat(customer.getId()).isEqualTo(customer2.getId());
    assertThat(customer.getName()).isEqualTo(customer2.getName());

    Customer.find.deleteById(customer.getId());
    awaitL2Cache();

    Customer notThere = Customer.find.byId(customer.getId());
    assertThat(notThere).isNull();
  }

  @Test
  public void test_update() {

    int rowsUpdated = Customer.find.updateToInactive("Frankie Who");
    logger.debug("updated {}", rowsUpdated);
  }

  @Test
  public void test_ormQuery() {

    ResetBasicData.reset();

    List<Customer> customers =
      Customer.find.byNameStatus("R", Customer.Status.NEW);

    assertThat(customers).isNotNull();
  }

  @Test
  public void test_nativeSingleAttribute() {

    ResetBasicData.reset();

    List<String> names = Customer.find.namesStartingWith("F");
    assertThat(names).isNotEmpty();
  }

  @Test
  public void test_finders_queryPlans() {

    ResetBasicData.reset();

    resetAllMetrics();

    List<Customer> customers = Customer.find.all();
    assertThat(customers).isNotEmpty();

    Customer customer = Customer.find.byId(1);
    assertThat(customer).isNotNull();
    Customer.find.byId(2);

    Customer.find.namesStartingWith("F");
    Customer.find.byNameStatus("Rob", Customer.Status.ACTIVE);
    Customer.find.totalCount();
    Customer.find.updateNames("Junk", 2000);
    Customer.find.byId(3);

    BasicMetricVisitor basic = server().getMetaInfoManager().visitBasic();

    List<MetaOrmQueryMetric> planStats = basic.getOrmQueryMetrics();
    assertThat(planStats.size()).isGreaterThan(4);

    for (MetaOrmQueryMetric planStat : planStats) {
      System.out.println(planStat);
    }

    for (MetaTimedMetric txnTimed : basic.getTimedMetrics()) {
      System.out.println(txnTimed);
    }
  }

}
