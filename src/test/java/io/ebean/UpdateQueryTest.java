package io.ebean;

import io.ebean.annotation.IgnorePlatform;
import io.ebean.annotation.Platform;
import io.ebean.meta.MetaOrmQueryMetric;
import io.ebean.meta.ServerMetrics;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;
import org.tests.model.basic.Country;
import org.tests.model.basic.Customer;
import org.tests.model.basic.EBasicWithUniqueCon;
import org.tests.model.basic.ResetBasicData;

import java.sql.Timestamp;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class UpdateQueryTest extends BaseTestCase {

  @Test
  public void basic() {

    resetAllMetrics();

    EbeanServer server = server();
    UpdateQuery<Customer> update = server.update(Customer.class);
    Query<Customer> query = update
      .set("status", Customer.Status.ACTIVE)
      .set("updtime", new Timestamp(System.currentTimeMillis()))
      .where()
      .eq("status", Customer.Status.NEW)
      .gt("id", 1000)
      .setLabel("updateActive");

    query.update();

    assertThat(query.getGeneratedSql()).contains("update o_customer set status=?, updtime=? where status = ? and id > ?");

    ServerMetrics metrics = collectMetrics();
    List<MetaOrmQueryMetric> ormQueryMetrics = metrics.getOrmQueryMetrics();
    assertThat(ormQueryMetrics).hasSize(1);
    assertThat(ormQueryMetrics.get(0).getType()).isEqualTo(Customer.class);
    assertThat(ormQueryMetrics.get(0).getLabel()).isEqualTo("updateActive");
  }

  @Test
  public void update() {

    ResetBasicData.reset();

    resetAllMetrics();

    UpdateQuery<Customer> update = server().update(Customer.class);

    LoggedSqlCollector.start();

    int rows = update
      .setRaw("status = status")
      .setLabel("updateAll")
      .update();


    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(1);
    assertThat(rows).isGreaterThan(0);

    assertThat(sql.get(0)).contains("update o_customer set status = status");

    ServerMetrics metrics = collectMetrics();
    List<MetaOrmQueryMetric> ormQueryMetrics = metrics.getOrmQueryMetrics();
    assertThat(ormQueryMetrics).hasSize(1);
    assertThat(ormQueryMetrics.get(0).getType()).isEqualTo(Customer.class);
    assertThat(ormQueryMetrics.get(0).getLabel()).isEqualTo("updateAll");
  }

  @Test
  public void query_asUpdate() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    int rows = server().find(Customer.class)
      .where()
      .gt("id", 1000)
      .asUpdate()
      .setRaw("status = status")
      .setLabel("asUpdate")
      .update();


    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(1);
    assertThat(rows).isEqualTo(0);

    assertThat(sql.get(0)).contains("update o_customer set status = status where id > ?");
  }

  @Test
  public void update_withTransactionBatch() {

    EbeanServer server = server();

    try (Transaction transaction = server.beginTransaction()) {
      transaction.setBatchMode(true);

      UpdateQuery<Customer> update = server.update(Customer.class);

      Query<Customer> query = update
        .set("status", Customer.Status.ACTIVE)
        .set("updtime", new Timestamp(System.currentTimeMillis()))
        .where()
        .eq("status", Customer.Status.NEW)
        .gt("id", 99999)
        .query();

      // update executes now regardless of transaction batch mode
      int rows = query.update();
      assertThat(rows).isEqualTo(0);

      transaction.commit();
    }
  }

  @Test
  @IgnorePlatform(Platform.SQLSERVER)
  public void withTableAlias() {

    EbeanServer server = server();
    UpdateQuery<Customer> update = server.update(Customer.class);
    Query<Customer> query = update
      .set("status", Customer.Status.ACTIVE)
      .set("updtime", new Timestamp(System.currentTimeMillis()))
      .where()
      .gt("id", 1000)
      .query();

    query.alias("cust");
    query.update();

    assertThat(query.getGeneratedSql()).contains("update o_customer cust set status=?, updtime=? where id > ?");
  }

  @Test
  public void withJoin() {

    if (isMySql()) {
      return;
    }
    EbeanServer server = server();

    Country nz = server.getReference(Country.class, "NZ");

    UpdateQuery<Customer> update = server.update(Customer.class);
    Query<Customer> query = update
      .set("status", Customer.Status.ACTIVE)
      .set("updtime", new Timestamp(System.currentTimeMillis()))
      .where()
      .eq("status", Customer.Status.NEW)
      .eq("billingAddress.country", nz)
      //.isEmpty("contacts")
      .gt("id", 1000)
      .query();

    query.update();

    assertThat(sqlOf(query)).contains("update o_customer set status=?, updtime=?  where id in (select t0.id from o_customer t0 left join o_address t1 on t1.id = t0.billing_address_id  where t0.status = ? and t1.country_code = ? and t0.id > ?)");
  }

  @Test
  public void whereIsEmpty() {

    EbeanServer server = server();

    Query<Customer> updateQuery = server
      .update(Customer.class)
      .set("status", Customer.Status.ACTIVE)
      .where()
      .isEmpty("contacts")
      .gt("id", 1000)
      .query();

    updateQuery.update();

    assertThat(updateQuery.getGeneratedSql()).contains("update o_customer set status=? where not exists (select 1 from contact x where x.customer_id = id) and id > ?");
  }

  @Test
  public void setNull() {

    EbeanServer server = server();

    Query<Customer> updateQuery = server
      .update(Customer.class)
      .setNull("status")
      .where()
      .gt("id", 1000)
      .query();

    updateQuery.update();

    assertThat(updateQuery.getGeneratedSql()).contains("update o_customer set status=null where id > ?");
  }

  @Test
  public void set_whenValueIsNull_expectNull() {

    EbeanServer server = server();

    Query<Customer> updateQuery = server
      .update(Customer.class)
      .set("status", null)
      .where()
      .gt("id", 1000)
      .query();

    updateQuery.update();

    assertThat(updateQuery.getGeneratedSql()).contains("update o_customer set status=null where id > ?");
  }

  @Test
  public void setExpression() {

    EbeanServer server = server();

    Query<Customer> updateQuery = server
      .update(Customer.class)
      .setRaw("status = coalesce(status, 'A')")
      .where()
      .gt("id", 1000)
      .query();

    updateQuery.update();

    assertThat(updateQuery.getGeneratedSql()).contains("update o_customer set status = coalesce(status, 'A') where id > ?");
  }

  @Test
  public void setExpression_withBind() {

    EbeanServer server = server();

    Query<Customer> updateQuery = server
      .update(Customer.class)
      .setRaw("status = coalesce(status, ?)", Customer.Status.ACTIVE)
      .where()
      .gt("id", 1000)
      .query();

    updateQuery.update();

    assertThat(updateQuery.getGeneratedSql()).contains("update o_customer set status = coalesce(status, ?) where id > ?");
  }

  @Test
  public void fluidSyntax() {

    EbeanServer server = server();

    int rows = server
      .update(Customer.class)
      .setRaw("status = coalesce(status, ?)", Customer.Status.ACTIVE)
      .where()
      .gt("id", 10000)
      .update();

    assertThat(rows).isEqualTo(0);
  }

  @Test
  public void updateQuery_withExplicitTransaction() {

    EbeanServer server = server();

    int rowsExprList;
    int rowsQuery;

    try (Transaction transaction = server.beginTransaction()) {

      rowsExprList = server
        .update(Customer.class)
        .setRaw("status = coalesce(status, ?)", Customer.Status.ACTIVE)
        .where()
        .gt("id", 10000)
        .update(transaction);

      rowsQuery = server
        .update(Customer.class)
        .setRaw("status = coalesce(status, ?)", Customer.Status.ACTIVE)
        .where()
        .gt("id", 10001)
        .query().update(transaction);

      transaction.commit();
    }

    assertThat(rowsExprList).isEqualTo(0);
    assertThat(rowsQuery).isEqualTo(0);
  }


  @Test
  public void deleteQuery_withExplicitTransaction() {

    EbeanServer server = server();

    int rowsExprList;
    int rowsQuery;

    try (Transaction transaction = server.beginTransaction()) {

      rowsExprList = server
        .update(Customer.class)
        .where()
        .gt("id", 10000)
        .delete(transaction);

      rowsQuery = server
        .update(Customer.class)
        .where()
        .gt("id", 10001)
        .query().delete(transaction);

      transaction.commit();
    }

    assertThat(rowsExprList).isEqualTo(0);
    assertThat(rowsQuery).isEqualTo(0);
  }

  @Test
  public void useViaEbean() {

    int rows = Ebean.update(Customer.class)
      .setRaw("status = coalesce(status, ?)", Customer.Status.ACTIVE)
      .where()
      .gt("id", 10000)
      .update();

    assertThat(rows).isEqualTo(0);
  }

  @Test(expected = DuplicateKeyException.class)
  public void exceptionTranslation() {

    newEbasicWithUnique("o1","other1_a");
    Integer id = newEbasicWithUnique("o2", "other1_b");

    Ebean.update(EBasicWithUniqueCon.class)
      .set("other", "other1_a")
      .set("otherOne", "other1_a")
      .where().idEq(id)
      .update();
  }

  private Integer newEbasicWithUnique(String name, String other) {
    EBasicWithUniqueCon b0 = new EBasicWithUniqueCon();
    b0.setName(name);
    b0.setOther(other);
    b0.setOtherOne(other);
    Ebean.save(b0);

    return b0.getId();
  }
}
