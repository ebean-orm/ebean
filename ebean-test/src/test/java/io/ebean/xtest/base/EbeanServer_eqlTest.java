package io.ebean.xtest.base;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import jakarta.persistence.PersistenceException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EbeanServer_eqlTest extends BaseTestCase {


  @Test
  public void basic() {

    ResetBasicData.reset();

    Query<Customer> query = server().createQuery(Customer.class).orderBy("id").setMaxRows(10);
    query.setMaxRows(100);
    query.findList();

    if (isSqlServer()) {
      assertSql(query).startsWith("select top 100 ");
      assertSql(query).endsWith("order by t0.id");
    } else if (isOracle() || isDb2()) {
      assertSql(query).contains(" fetch next 100 rows only");
    } else {
      assertSql(query).endsWith("order by t0.id limit 100");
    }
  }

  @Test
  public void basic_via_Ebean_defaultServer() {

    ResetBasicData.reset();

    Query<Customer> query = server().createQuery(Customer.class).orderBy("id").setMaxRows(10);
    query.findList();

    if (isSqlServer()) {
      assertSql(query).startsWith("select top 10 ");
      assertSql(query).endsWith("order by t0.id");
    } else if (isOracle() || isDb2()) {
      assertSql(query).contains(" fetch next 10 rows only");
    } else {
      assertSql(query).endsWith("order by t0.id limit 10");
    }
  }

  @Test
  public void basic_limit_offset1() {

    ResetBasicData.reset();

    Query<Customer> query = server().createQuery(Customer.class).orderBy("id").setMaxRows(10).setFirstRow(3);
    query.findList();

    if (isSqlServer()) {
      assertSql(query).endsWith("order by t0.id offset 3 rows fetch next 10 rows only");
    } else if (isOracle() || isDb2()) {
      assertSql(query).contains("offset 3 rows fetch next 10 rows only");
    } else {
      assertSql(query).endsWith("order by t0.id limit 10 offset 3");
    }

  }

  @Test
  public void basic_limit_offset2() {

    ResetBasicData.reset();

    Query<Customer> query = server().createQuery(Customer.class).orderBy("name");
    query.setMaxRows(10);
    query.setFirstRow(3);
    query.findList();

    if (isSqlServer()) {
      assertSql(query).endsWith("order by t0.name offset 3 rows fetch next 10 rows only");
    } else if (isOracle() || isDb2()) {
      assertSql(query).contains("offset 3 rows fetch next 10 rows only");
    } else {
      assertSql(query).endsWith("order by t0.name limit 10 offset 3");
    }

    // check also select count(*)
    LoggedSql.start();
    query.findCount();
    List<String> sql = LoggedSql.stop();
    assertThat(sql.get(0)).startsWith("select count(*) from o_customer t0;");
  }

  @Test
  public void basic_limit_offset2_with_id() {

    ResetBasicData.reset();

    Query<Customer> query = server().createQuery(Customer.class).orderBy("name");
    query.setMaxRows(10);
    query.setFirstRow(3);
    query.orderById(true);
    query.findList();

    if (isSqlServer()) {
      assertSql(query).endsWith("order by t0.name, t0.id offset 3 rows fetch next 10 rows only");
    } else if (isOracle() || isDb2()) {
      assertSql(query).contains("offset 3 rows fetch next 10 rows only");
    } else {
      assertSql(query).endsWith("order by t0.name, t0.id limit 10 offset 3");
    }

    // check also select count(*)
    LoggedSql.start();
    query.findCount();
    List<String> sql = LoggedSql.stop();
    assertThat(sql.get(0)).startsWith("select count(*) from o_customer t0;");

  }

  @Test
  public void basic_limit_offset3() {

    ResetBasicData.reset();

    Query<Customer> query = DB.createQuery(Customer.class);
    query.setMaxRows(10);
    query.setFirstRow(3);
    if (isSqlServer()) {
      query.orderBy("id");
    }
    query.findList();

    if (isSqlServer()) {
      assertSql(query).endsWith("from o_customer t0 order by t0.id offset 3 rows fetch next 10 rows only");
    } else if (isOracle() || isDb2()) {
      assertSql(query).contains("offset 3 rows fetch next 10 rows only");
    } else {
      assertSql(query).endsWith("from o_customer t0 limit 10 offset 3");
    }
  }

  @Test
  public void basic_limit_offset4() {

    ResetBasicData.reset();

    Query<Customer> query = DB.createQuery(Customer.class);
    query.setMaxRows(10);
    query.findList();

    if (isSqlServer()) {
      assertSql(query).startsWith("select top 10 ");
    } else if (isOracle() || isDb2()) {
      assertSql(query).contains("fetch next 10 rows only");
    } else {
      assertSql(query).endsWith("limit 10");
    }
  }

  @Test
  public void orderBy_override() {

    ResetBasicData.reset();

    Query<Customer> query = server().createQuery(Customer.class).orderBy("id");

    // use clear() and then effectively override the orderBy clause
    query.orderBy().clear().asc("name");
    query.findList();

    assertSql(query).contains("order by t0.name");
  }


  @Test
  public void namedParams() {

    ResetBasicData.reset();

    Query<Customer> query = server().createQuery(Customer.class)
      .where().startsWith("name", "Ro")
      .orderBy("name")
      .query();

    query.findList();

    assertSql(query).contains("where t0.name like ");
  }

}
