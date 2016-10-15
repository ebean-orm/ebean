package com.avaje.tests.query.other;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.sql.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestQuerySingleAttribute extends BaseTestCase {


  @Test
  public void exampleUsage() {

    ResetBasicData.reset();

    List<String> names =
        Ebean.find(Customer.class)
          .setDistinct(true)
          .select("name")
          .where().eq("status", Customer.Status.NEW)
          .orderBy().asc("name")
          .setMaxRows(100)
          .findSingleAttributeList();

    assertThat(names).isNotNull();
  }

  @Test
  public void exampleUsage_otherType() {

    ResetBasicData.reset();

    List<Date> dates =
        Ebean.find(Customer.class)
            .setDistinct(true)
            .select("anniversary")
            .where().isNotNull("anniversary")
            .orderBy().asc("anniversary")
            .findSingleAttributeList();

    assertThat(dates).isNotNull();
  }

  @Test
  public void withOrderBy() {

    Query<Customer> query =
        Ebean.find(Customer.class)
            .setDistinct(true)
            .select("name")
            .where().eq("status", Customer.Status.NEW)
            .orderBy().asc("name")
            .setMaxRows(100);

    query.findSingleAttributeList();
    assertThat(sqlOf(query)).contains("select distinct t0.name from o_customer t0 where t0.status = ?  order by t0.name ");
  }

  @Test
  public void basic() {

    ResetBasicData.reset();

    Query<Customer> query = Ebean.find(Customer.class).select("name");

    List<String> names = query.findSingleAttributeList();//String.class);

    assertThat(sqlOf(query)).contains("select t0.name from o_customer t0");
    assertThat(names).isNotNull();
  }

  @Test
  public void distinctAndWhere() {

    ResetBasicData.reset();

    Query<Customer> query = Ebean.find(Customer.class)
        .setDistinct(true)
        .select("name")
        .where().eq("status", Customer.Status.NEW)
        .query();

    List<String> names = query.findSingleAttributeList();

    assertThat(sqlOf(query)).contains("select distinct t0.name from o_customer t0 where t0.status = ? ");
    assertThat(names).isNotNull();
  }

  @Test
  public void distinctWhereWithJoin() {

    ResetBasicData.reset();

    Query<Customer> query = Ebean.find(Customer.class)
        .setDistinct(true)
        .select("name")
        .where().eq("status", Customer.Status.NEW)
        .istartsWith("billingAddress.city", "auck")
        .query();

    List<String> names = query.findSingleAttributeList();

    assertThat(sqlOf(query)).contains("select distinct t0.name from o_customer t0 left join o_address t1 on t1.id = t0.billing_address_id  where t0.status = ?  and lower(t1.city) like ?");
    assertThat(names).isNotNull();
  }


  @Test
  public void queryPlan_expect_differentPlans() {

    ResetBasicData.reset();

    Query<Customer> query = Ebean.find(Customer.class).select("name");
    query.findSingleAttributeList();
    assertThat(sqlOf(query)).contains("select t0.name from o_customer t0");

    Query<Customer> query2 = Ebean.find(Customer.class).select("name");
    query2.findList();
    assertThat(sqlOf(query2, 1)).contains("select t0.id, t0.name from o_customer t0");
  }
}
