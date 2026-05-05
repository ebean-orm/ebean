package org.tests.query;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import static org.assertj.core.api.Assertions.assertThat;

public class TestExprNestedDisjunction extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    java.sql.Date onAfter = java.sql.Date.valueOf("2009-08-31");

    Query<Customer> q = DB.find(Customer.class).where()
      .disjunction()
      .conjunction().startsWith("name", "r").eq("anniversary", onAfter).endJunction()
      .conjunction().eq("status", Customer.Status.ACTIVE).gt("id", 0).endJunction()
      .orderBy().asc("name");

    q.findList();
    String s = q.getGeneratedSql();

    assertThat(s).contains("(t0.name like ");
    assertThat(s).contains(" and t0.anniversary = ?) or (t0.status = ? and t0.id > ?)");
  }


  @Test
  public void test_nested_and() {

    ResetBasicData.reset();

    java.sql.Date onAfter = java.sql.Date.valueOf("2009-08-31");

    Query<Customer> q = DB.find(Customer.class)
      .where()
      .or()
      .and()
      .startsWith("name", "r").eq("anniversary", onAfter).endAnd()
      .and()
      .eq("status", Customer.Status.ACTIVE).gt("id", 0).endAnd()
      .orderBy().asc("name");

    q.findList();
    String s = q.getGeneratedSql();

    assertThat(s).contains("(t0.name like ");
    assertThat(s).contains(" and t0.anniversary = ?) or (t0.status = ? and t0.id > ?)");
  }

  @Test
  public void test_not() {

    ResetBasicData.reset();

    java.sql.Date onAfter = java.sql.Date.valueOf("2009-08-31");

    Query<Customer> q = DB.find(Customer.class)
      .where()
      .not()
      .gt("id", 1)
      .eq("anniversary", onAfter)
      .endNot()
      .orderBy().asc("name");

    q.findList();
    String s = q.getGeneratedSql();

    assertThat(s).contains("where not (t0.id > ? and t0.anniversary = ?) order by t0.name");
  }

  @Test
  public void test_not_nested() {

    ResetBasicData.reset();

    java.sql.Date onAfter = java.sql.Date.valueOf("2009-08-31");

    Query<Customer> q = DB.find(Customer.class)
      .where()
      .or()
      .eq("status", Customer.Status.ACTIVE)
      .not()
      .gt("id", 1)
      .eq("anniversary", onAfter)
      .orderBy().asc("name");

    q.findList();
    String s = q.getGeneratedSql();

    assertThat(s).contains("where (t0.status = ? or not (t0.id > ? and t0.anniversary = ?))");
  }

  @Test
  public void test_not_nested_with_endNot() {

    ResetBasicData.reset();

    java.sql.Date onAfter = java.sql.Date.valueOf("2009-08-31");

    Query<Customer> q = DB.find(Customer.class)
      .where()
      .or()
      .eq("status", Customer.Status.ACTIVE)
      .not()
      .gt("id", 1)
      .eq("anniversary", onAfter)
      .endNot()
      .endOr()
      .orderBy().asc("name");

    q.findList();
    String s = q.getGeneratedSql();

    assertThat(s).contains("where (t0.status = ? or not (t0.id > ? and t0.anniversary = ?))");
  }
}
