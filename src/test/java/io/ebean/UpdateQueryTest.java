package io.ebean;

import io.ebean.annotation.IgnorePlatform;
import io.ebean.annotation.Platform;
import io.ebean.meta.BasicMetricVisitor;
import io.ebean.meta.MetaOrmQueryMetric;
import org.junit.Test;
import org.tests.model.basic.Country;
import org.tests.model.basic.Customer;

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

    assertThat(query.getGeneratedSql()).contains("update o_customer set status=?, updtime=? where status = ?  and id > ?");

    BasicMetricVisitor basic = visitMetricsBasic();
    List<MetaOrmQueryMetric> ormQueryMetrics = basic.getOrmQueryMetrics();
    assertThat(ormQueryMetrics).hasSize(1);
    assertThat(ormQueryMetrics.get(0).getType()).isEqualTo(Customer.class);
    assertThat(ormQueryMetrics.get(0).getLabel()).isEqualTo("updateActive");
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

    assertThat(sqlOf(query)).contains("update o_customer set status=?, updtime=?  where id in (select t0.id from o_customer t0 left join o_address t1 on t1.id = t0.billing_address_id  where t0.status = ?  and t1.country_code = ?  and t0.id > ? )");
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
  public void useViaEbean() {

    int rows = Ebean.update(Customer.class)
      .setRaw("status = coalesce(status, ?)", Customer.Status.ACTIVE)
      .where()
      .gt("id", 10000)
      .update();

    assertThat(rows).isEqualTo(0);
  }
}
