package io.ebean.xtest.base;

import io.ebean.*;
import io.ebean.xtest.BaseTestCase;
import io.ebean.xtest.ForPlatform;
import io.ebean.xtest.IgnorePlatform;
import io.ebean.annotation.Platform;
import io.ebean.meta.MetaQueryMetric;
import io.ebean.meta.ServerMetrics;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Country;
import org.tests.model.basic.Customer;
import org.tests.model.basic.EBasicWithUniqueCon;
import org.tests.model.basic.ResetBasicData;

import java.sql.Timestamp;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UpdateQueryTest extends BaseTestCase {

  @Test
  public void basic() {

    resetAllMetrics();

    Database server = server();
    UpdateQuery<Customer> update = server.update(Customer.class);
    Query<Customer> query = update
      .set("status", Customer.Status.ACTIVE)
      .set("updtime", new Timestamp(System.currentTimeMillis()))
      .where()
      .eq("status", Customer.Status.NEW)
      .gt("id", 1000)
      .setLabel("updateActive");

    query.update();

    assertSql(query).contains("update o_customer set status=?, updtime=? where status = ? and id > ?");

    ServerMetrics metrics = collectMetrics();
    List<MetaQueryMetric> ormQueryMetrics = metrics.queryMetrics();
    assertThat(ormQueryMetrics).hasSize(1);
    assertThat(ormQueryMetrics.get(0).type()).isEqualTo(Customer.class);
    assertThat(ormQueryMetrics.get(0).label()).isEqualTo("updateActive");
  }

  @Test
  public void update() {

    ResetBasicData.reset();

    resetAllMetrics();

    UpdateQuery<Customer> update = server().update(Customer.class);

    LoggedSql.start();

    int rows = update
      .setRaw("status = status")
      .setLabel("updateAll")
      .update();


    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(rows).isGreaterThan(0);

    assertSql(sql.get(0)).contains("update o_customer set status = status");

    ServerMetrics metrics = collectMetrics();
    List<MetaQueryMetric> ormQueryMetrics = metrics.queryMetrics();
    assertThat(ormQueryMetrics).hasSize(1);
    assertThat(ormQueryMetrics.get(0).type()).isEqualTo(Customer.class);
    assertThat(ormQueryMetrics.get(0).label()).isEqualTo("updateAll");
  }

  @Test
  public void query_asUpdate() {

    ResetBasicData.reset();

    LoggedSql.start();

    int rows = server().find(Customer.class)
      .where()
      .gt("id", 1000)
      .asUpdate()
      .setRaw("status = status")
      .setLabel("asUpdate")
      .update();


    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(rows).isEqualTo(0);

    assertSql(sql.get(0)).contains("update o_customer set status = status where id > ?");
  }

  @Test
  public void query_asUpdate_idIn() {

    ResetBasicData.reset();
    LoggedSql.start();

    int rows = server().find(Customer.class)
      .where()
      .idIn(1000, 1001, 1002)
      .asUpdate()
      .setRaw("status = ?", "A")
      .setLabel("asUpdateByIds")
      .update();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(rows).isEqualTo(0);

    if (isPostgresCompatible()) {
      assertSql(sql.get(0)).contains("update o_customer set status = ? where id = any(?)");
    } else {
      assertSql(sql.get(0)).contains("update o_customer set status = ? where id in (?,?,?,?,?)"); // bind padding to 5
    }
  }

  @Test
  public void update_withTransactionBatch() {

    Database server = server();

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

    Database server = server();
    UpdateQuery<Customer> update = server.update(Customer.class);
    Query<Customer> query = update
      .set("status", Customer.Status.ACTIVE)
      .set("updtime", new Timestamp(System.currentTimeMillis()))
      .where()
      .gt("id", 1000)
      .query();

    query.alias("cust");
    query.update();

    assertSql(query).contains("update o_customer cust set status=?, updtime=? where id > ?");
  }

  @IgnorePlatform({Platform.MYSQL, Platform.MARIADB})
  @Test
  public void withJoin() {

    Database server = server();

    Country nz = server.reference(Country.class, "NZ");

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

    assertThat(sqlOf(query)).contains("update o_customer set status=?, updtime=?  where id in (select t0.id from o_customer t0 left join o_address t1 on t1.id = t0.billing_address_id where t0.status = ? and t1.country_code = ? and t0.id > ?)");
  }

  @ForPlatform({Platform.H2, Platform.POSTGRES})
  @Test
  public void withJoinAndLimit() {

    Database server = server();

    Country nz = server.reference(Country.class, "NZ");

    LoggedSql.start();

    server.update(Customer.class)
      .set("status", Customer.Status.ACTIVE)
      .where()
      .eq("billingAddress.country", nz)
      .gt("id", 1000)
      .setMaxRows(100)
      .update();

    final List<String> sql = LoggedSql.stop();
    assertSql(sql.get(0)).contains("update o_customer set status=?  where id in (select t0.id from o_customer t0 left join o_address t1 on t1.id = t0.billing_address_id where t1.country_code = ? and t0.id > ? limit 100)");
  }

  @ForPlatform({Platform.H2, Platform.POSTGRES, Platform.MYSQL, Platform.MARIADB})
  @Test
  public void simpleWithLimit() {

    Database server = server();

    LoggedSql.start();

    server.update(Customer.class)
      .set("status", Customer.Status.ACTIVE)
      .where()
      .gt("id", 1000)
      .setMaxRows(100)
      .update();

    final List<String> sql = LoggedSql.stop();
    if (isMySql() || isH2() || isMariaDB()) {
      assertSql(sql.get(0)).contains("update o_customer set status=? where id > ? limit 100");
    } else {
      assertSql(sql.get(0)).contains("update o_customer set status=?  where id in (select t0.id from o_customer t0 where t0.id > ? limit 100)");
    }
  }

  @Test
  public void whereIsEmpty() {

    Database server = server();

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

    Database server = server();

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

    Database server = server();

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

    Database server = server();

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

    Database server = server();

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

    Database server = server();

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

    Database server = server();

    int rowsExprList;
    int rowsQuery;

    try (Transaction transaction = server.beginTransaction()) {

      rowsExprList = server
        .update(Customer.class)
        .setRaw("status = coalesce(status, ?)", Customer.Status.ACTIVE)
        .where()
        .gt("id", 10000)
        .usingTransaction(transaction).update();

      rowsQuery = server
        .update(Customer.class)
        .setRaw("status = coalesce(status, ?)", Customer.Status.ACTIVE)
        .where()
        .gt("id", 10001)
        .usingTransaction(transaction).update();

      transaction.commit();
    }

    assertThat(rowsExprList).isEqualTo(0);
    assertThat(rowsQuery).isEqualTo(0);
  }


  @Test
  public void deleteQuery_withExplicitTransaction() {

    Database server = server();

    int rowsExprList;
    int rowsQuery;

    try (Transaction transaction = server.beginTransaction()) {

      rowsExprList = server
        .update(Customer.class)
        .where()
        .gt("id", 10000)
        .usingTransaction(transaction).delete();

      rowsQuery = server
        .update(Customer.class)
        .where()
        .gt("id", 10001)
        .usingTransaction(transaction).delete();

      transaction.commit();
    }

    assertThat(rowsExprList).isEqualTo(0);
    assertThat(rowsQuery).isEqualTo(0);
  }

  @Test
  public void useViaEbean() {

    int rows = DB.update(Customer.class)
      .setRaw("status = coalesce(status, ?)", Customer.Status.ACTIVE)
      .where()
      .gt("id", 10000)
      .update();

    assertThat(rows).isEqualTo(0);
  }

  @Test
  public void exceptionTranslation() {
    newEbasicWithUnique("o1","other1_a");
    Integer id = newEbasicWithUnique("o2", "other1_b");

    assertThrows(DuplicateKeyException.class, () -> {
      DB.update(EBasicWithUniqueCon.class)
        .set("other", "other1_a")
        .set("otherOne", "other1_a")
        .where().idEq(id)
        .update();
    });
  }

  private Integer newEbasicWithUnique(String name, String other) {
    EBasicWithUniqueCon b0 = new EBasicWithUniqueCon();
    b0.setName(name);
    b0.setOther(other);
    b0.setOtherOne(other);
    DB.save(b0);

    return b0.getId();
  }
}
