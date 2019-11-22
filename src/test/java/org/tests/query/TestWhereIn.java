package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import org.junit.Test;
import org.tests.model.basic.Country;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;


public class TestWhereIn extends BaseTestCase {

  @Test
  public void testInVarchar() {

    ResetBasicData.reset();

    Query<Country> query = DB.find(Country.class)
      .where().in("code", "NZ", "AU")
      .query();

    query.findList();
    platformAssertIn(sqlOf(query), "where t0.code");
  }

  @Test
  public void testNotInVarchar() {

    ResetBasicData.reset();

    Query<Country> query = DB.find(Country.class)
      .where().notIn("code", "NZ", "SA", "US")
      .query();

    query.findList();
    platformAssertNotIn(sqlOf(query), "where t0.code");
  }

  @Test
  public void testInOrEmpty_expect_noJoinWhenEmpty() {

    ResetBasicData.reset();

    Query<Order> query = DB.find(Order.class)
      .select("id")
      .where().inOrEmpty("customer.billingAddress.id", new ArrayList<>()).query();

    query.findList();
    assertThat(sqlOf(query)).isEqualTo("select t0.id from o_order t0");
  }

  @Test
  public void testInOrEmpty_expect_joinWhenNotEmpty() {

    ResetBasicData.reset();

    Query<Order> query = DB.find(Order.class)
      .select("id")
      .where().inOrEmpty("customer.billingAddress.id", Arrays.asList(1)).query();

    query.findList();
    assertThat(sqlOf(query)).contains("select t0.id from o_order t0 join o_customer t1 on t1.id = t0.kcustomer_id  where t1.billing_address_id ");
  }


  @Test
  public void testInOrEmpty_when_null() {

    ResetBasicData.reset();

    Query<Country> query = DB.find(Country.class)
      .where().inOrEmpty("code", null).query();

    query.findList();
    assertThat(sqlOf(query)).isEqualTo("select t0.code, t0.name from o_country t0");
  }

  @Test
  public void testInOrEmpty_when_empty() {

    ResetBasicData.reset();

    Query<Country> query = DB.find(Country.class)
      .where().inOrEmpty("code", new ArrayList<>()).query();

    query.findList();
    assertThat(sqlOf(query)).isEqualTo("select t0.code, t0.name from o_country t0");
  }

  @Test
  public void testIn_when_empty() {

    ResetBasicData.reset();

    Query<Country> query = DB.find(Country.class)
      .where().in("code", new ArrayList<>()).query();

    query.findList();
    assertThat(sqlOf(query)).contains("where 1=0");
  }

  @Test
  public void testIn_when_null() {

    ResetBasicData.reset();

    Query<Country> query = DB.find(Country.class)
      .where().in("code", (Collection)null).query();

    query.findList();
    assertThat(sqlOf(query)).contains("where 1=0");
  }

  @Test
  public void testNotIn_when_empty() {

    ResetBasicData.reset();

    Query<Country> query = DB.find(Country.class)
      .where().notIn("code", new ArrayList<>()).query();

    query.findList();
    assertThat(sqlOf(query)).contains("where 1=1");
  }

  @Test
  public void testNotIn_when_null() {

    ResetBasicData.reset();

    Query<Country> query = DB.find(Country.class)
      .where().notIn("code", (Collection)null).query();

    query.findList();
    assertThat(sqlOf(query)).contains("where 1=1");
  }
}
