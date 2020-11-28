package org.tests.query.finder;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Transaction;
import io.ebean.meta.MetaQueryMetric;
import io.ebean.meta.MetaQueryPlan;
import io.ebean.meta.MetaTimedMetric;
import io.ebean.meta.QueryPlanInit;
import io.ebean.meta.QueryPlanRequest;
import io.ebean.meta.ServerMetrics;
import io.ebean.meta.SortMetric;
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
  public void a_runFirst_metricsAsJson_sqlInInitialCollectionOnly() {

    ResetBasicData.reset();

    runQueries();

    StringBuilder buffer0 = new StringBuilder();
    server().getMetaInfoManager()
      .collectMetricsAsJson()
      .withHeader(false)
      .write(buffer0);

    String json0 = buffer0.toString();
    System.out.println(json0);
    assertThat(json0).contains("\"name\":\"txn.main\"");
    assertThat(json0).contains("\"name\":\"orm.Customer.findList\"");
    //assertThat(json0).contains("\"sql\":\"select t0.id, t0.status, t0.name");

    runQueries();

    StringBuilder buffer1 = new StringBuilder();
    server().getMetaInfoManager()
      .collectMetricsAsJson()
      .withHeader(false)
      .write(buffer1);

    String json1 = buffer1.toString();
    System.out.println(json1);
    assertThat(json1).contains("\"name\":\"txn.main\"");
    assertThat(json1).contains("\"name\":\"orm.Customer.findList\"");
    assertThat(json1).doesNotContain("\"sql\":\"select t0.id, t0.status, t0.name");

  }

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
    List<Customer> list = DB.find(Customer.class).findList();

    assertThat(all.size()).isEqualTo(list.size());

    Customer customer = all.get(0);

    Customer customer1 = Customer.find.byId(customer.getId());

    assertThat(customer.getId()).isEqualTo(customer1.getId());
    assertThat(customer.getName()).isEqualTo(customer1.getName());

    assertThat(Customer.find.db().getName()).isEqualTo(DB.getDefault().getName());

  }

  @Test
  public void currentTransaction() {

    DB.beginTransaction();
    try {
      Transaction t1 = DB.currentTransaction();
      Transaction t2 = Customer.find.currentTransaction();
      assertThat(t2).isSameAs(t1);

    } finally {
      DB.endTransaction();
    }
  }

  @Test
  public void flush() {

    Transaction transaction = DB.beginTransaction();
    try {
      Customer.find.currentTransaction().setBatchMode(true);

      LoggedSqlCollector.start();
      EBasic b = new EBasic("junk");
      DB.save(b);

      assertTrue(LoggedSqlCollector.current().isEmpty());
      Customer.find.flush();

      List<String> sql = LoggedSqlCollector.stop();
      assertSql(sql.get(0)).contains("insert into e_basic");
    } finally {
      transaction.end();
    }
  }

  @Test
  public void test_byName_deleteById() {

    Customer customer = new Customer();
    customer.setName("Newbie-879879897");

    DB.save(customer);
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

    // the server has some plans
    runQueries();

    // enable query plan bind capture on all plans threshold 100 micros
    QueryPlanInit init = new QueryPlanInit();
    init.setAll(true);
    init.setThresholdMicros(100);
    final List<MetaQueryPlan> appliedToPlans = server().getMetaInfoManager().queryPlanInit(init);
    assertThat(appliedToPlans.size()).isGreaterThan(4);

    // will collect bind captures
    runQueries();

    ServerMetrics metrics = server().getMetaInfoManager().collectMetrics();

    List<MetaQueryMetric> planStats = metrics.getQueryMetrics();
    assertThat(planStats.size()).isGreaterThan(4);

    for (MetaQueryMetric planStat : planStats) {
      System.out.println(planStat);
    }

    for (MetaTimedMetric txnTimed : metrics.getTimedMetrics()) {
      System.out.println(txnTimed);
    }

    // obtains db query plans ...
    QueryPlanRequest request = new QueryPlanRequest();
    List<MetaQueryPlan> plans = server().getMetaInfoManager().queryPlanCollectNow(request);
    assertThat(plans).isNotEmpty();

    for (MetaQueryPlan plan : plans) {
      System.out.println(plan);
    }
  }

  @Test
  public void test_metricsAsJson_withAll() {

    ResetBasicData.reset();

    runQueries();

    String metricsJson = server().getMetaInfoManager()
      .collectMetricsAsJson()
      .withHash(true)
      .withExtraAttributes(true)
      .withNewLine(true)
      .withSort(SortMetric.TOTAL)
      .json();

    assertThat(metricsJson).contains("\"name\":\"txn.main\"");
    assertThat(metricsJson).contains("\"name\":\"orm.Customer.findList\"");
    assertThat(metricsJson).contains("\"loc\":\"CustomerFinder.byNameStatus(CustomerFinder.java:44)\"");
    if (isH2() || isPostgres()) {
      assertThat(metricsJson).contains("\"hash\":\"cc20eb930403cfd418db2d0475c6e26a\"");
      assertThat(metricsJson).contains("\"sql\":\"select t0.id, t0.status,");
    }
  }

  @Test
  public void test_metricsAsJson_minimal() {

    ResetBasicData.reset();

    runQueries();

    String metricsJson = server().getMetaInfoManager()
      .collectMetricsAsJson()
      .withHash(false)
      .withExtraAttributes(false)
      .withNewLine(false)
      .withSort(null)
      .json();

    assertThat(metricsJson).contains("\"name\":\"txn.main\"");
    assertThat(metricsJson).contains("\"name\":\"orm.Customer.findList\"");
    assertThat(metricsJson).doesNotContain("\"loc\":");
    assertThat(metricsJson).doesNotContain("\"hash\":");
    assertThat(metricsJson).doesNotContain("\"sql\":");
  }

  @Test
  public void test_metricsAsJson_write() {

    ResetBasicData.reset();

    runQueries();

    StringBuilder buffer = new StringBuilder();
    server().getMetaInfoManager()
      .collectMetricsAsJson()
      .withHeader(false)
      .write(buffer);

    String metricsJson = buffer.toString();
    assertThat(metricsJson).contains("\"name\":\"txn.main\"");
    assertThat(metricsJson).contains("\"name\":\"orm.Customer.findList\"");
  }

  @Test
  public void test_metricsAsJson_writeWithHeader() {

    ResetBasicData.reset();

    runQueries();

    StringBuilder buffer = new StringBuilder();
    server().getMetaInfoManager()
      .collectMetricsAsJson()
      .withHeader(true)
      .write(buffer);

    String metricsJson = buffer.toString();
    assertThat(metricsJson).contains(" \"metrics\":[");
    assertThat(metricsJson).contains("\"name\":\"txn.main\"");
    assertThat(metricsJson).contains("\"name\":\"orm.Customer.findList\"");
  }

  private void runQueries() {
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
    DB.find(Customer.class)
      .setLabel("someDeleteQuery")
      .where().eq("name", "JunkNotExists")
      .delete();
  }

}
