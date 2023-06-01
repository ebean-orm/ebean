package org.tests.query.finder;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Transaction;
import io.ebean.xtest.IgnorePlatform;
import io.ebean.annotation.Platform;
import io.ebean.meta.*;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.EBasic;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestCustomerFinder extends BaseTestCase {

  @Test
  public void a_runFirst_metricsAsJson_sqlInInitialCollectionOnly() {

    ResetBasicData.reset();

    runQueries();

    StringBuilder buffer0 = new StringBuilder();
    DB.getDefault().metaInfo()
      .collectMetrics().asJson()
      .withHeader(false)
      .write(buffer0);

    String json0 = buffer0.toString();
    System.out.println(json0);
    assertThat(json0).contains("\"name\":\"txn.main\"");
    assertThat(json0).contains("\"name\":\"orm.Customer.findList\"");
    //assertThat(json0).contains("\"sql\":\"select t0.id, t0.status, t0.name");

    runQueries();

    StringBuilder buffer1 = new StringBuilder();
    DB.getDefault().metaInfo()
      .collectMetrics().asJson()
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

    assertThat(Customer.find.db().name()).isEqualTo(DB.getDefault().name());
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

      LoggedSql.start();
      EBasic b = new EBasic("junk");
      DB.save(b);

      assertTrue(LoggedSql.collect().isEmpty());
      Customer.find.flush();

      List<String> sql = LoggedSql.stop();
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
  @IgnorePlatform(Platform.DB2) // no query plans yet, for DB2
  public void test_finders_queryPlans() {

    ResetBasicData.reset();

    // change default collect query plan threshold to 20 micros
    QueryPlanInit init0 = new QueryPlanInit();
    init0.setAll(true);
    init0.thresholdMicros(20);
    final List<MetaQueryPlan> plans = server().metaInfo().queryPlanInit(init0);
    assertThat(plans.size()).isGreaterThan(1);

    // the server has some plans
    runQueries();

    // change query plan threshold to 10 micros
    QueryPlanInit init = new QueryPlanInit();
    init.setAll(true);
    init.thresholdMicros(10);
    final List<MetaQueryPlan> appliedToPlans = server().metaInfo().queryPlanInit(init);
    assertThat(appliedToPlans.size()).isGreaterThan(4);

    // run queries again
    runQueries();

    ServerMetrics metrics = DB.getDefault().metaInfo().collectMetrics();

    List<MetaQueryMetric> planStats = metrics.queryMetrics();
    assertThat(planStats.size()).isGreaterThan(4);

    for (MetaQueryMetric planStat : planStats) {
      System.out.println(planStat);
    }

    for (MetaTimedMetric txnTimed : metrics.timedMetrics()) {
      System.out.println(txnTimed);
    }

    // obtains db query plans ...
    QueryPlanRequest request = new QueryPlanRequest();
    // collect max 1000 plans (use something more like 10)
    request.maxCount(1_000);
    // don't collect any more plans if used 10 secs
    request.maxTimeMillis(10_000);
    List<MetaQueryPlan> plans0 = server().metaInfo().queryPlanCollectNow(request);
    assertThat(plans0).isNotEmpty();

    for (MetaQueryPlan plan : plans0) {
      logger.info("queryPlan label:{}, queryTimeMicros:{} captureMicros:{} whenCaptured:{} captureCount:{} loc:{} sql:{} bind:{} plan:{}",
        plan.label(), plan.queryTimeMicros(), plan.captureMicros(), plan.whenCaptured(), plan.captureCount(), plan.profileLocation(),
        plan.sql(), plan.bind(), plan.plan());
      System.out.println(plan);
    }

    //DB.getBackgroundExecutor().scheduleWithFixedDelay(...)
  }

  @Test
  public void test_metricsAsJson_withAll() {

    ResetBasicData.reset();

    runQueries();

    String metricsJson = server().metaInfo()
      .collectMetrics().asJson()
      .withHash(true)
      .withExtraAttributes(true)
      .withNewLine(true)
      .withSort(SortMetric.TOTAL)
      .json();

    assertThat(metricsJson).contains("\"name\":\"txn.main\"");
    assertThat(metricsJson).contains("\"name\":\"orm.Customer.findList\"");
    assertThat(metricsJson).contains("\"loc\":\"org.tests.model.basic.finder.CustomerFinder.byNameStatus\"");
    if (isH2() || isPostgresCompatible()) {
      assertThat(metricsJson).contains("\"hash\":\"de3affa5b4bff07e19c1c012590dcde6\"");
      assertThat(metricsJson).contains("\"sql\":\"select t0.id, t0.status,");
    }
  }

  @Test
  public void test_metricsAsJson_minimal() {

    ResetBasicData.reset();

    runQueries();

    String metricsJson = server().metaInfo()
      .collectMetrics().asJson()
      .withHash(false)
      .withExtraAttributes(false)
      .withNewLine(false)
      .withSort(null)
      .json();

    assertThat(metricsJson).contains("\"name\":\"txn.main\"");
    assertThat(metricsJson).contains("\"name\":\"orm.Customer.findList\"");
    assertThat(metricsJson).doesNotContain("\"loc\":");
    assertThat(metricsJson).doesNotContain("\"sqlHash\":");
    assertThat(metricsJson).doesNotContain("\"sql\":");
  }

  @Test
  public void test_metricsAsJson_write() {

    ResetBasicData.reset();

    runQueries();

    StringBuilder buffer = new StringBuilder();
    server().metaInfo()
      .collectMetrics().asJson()
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
    server().metaInfo()
      .collectMetrics().asJson()
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
