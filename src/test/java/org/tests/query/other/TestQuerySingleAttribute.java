package org.tests.query.other;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;
import org.avaje.test.model.rawsql.inherit.ChildA;
import org.avaje.test.model.rawsql.inherit.Data;
import org.avaje.test.model.rawsql.inherit.EUncle;
import org.junit.Ignore;
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
  
  @Test
  public void distinctOnIdProperty(){
    Query<Customer> query = Ebean.find(Customer.class)
        .setDistinct(true)
        .select("id")
        .setMaxRows(100);

    List<String> ids = query.findSingleAttributeList();
    if (isSqlServer()) {
      assertThat(sqlOf(query)).contains("select distinct top 100 t0.id from o_customer t0");
    } else {
      assertThat(sqlOf(query)).contains("select distinct t0.id from o_customer t0 limit 100");
    }
    assertThat(ids).isNotEmpty();
  }

  @Test
  public void distinctWithFetch() {

    ResetBasicData.reset();

    Query<Customer> query = Ebean.find(Customer.class)
        .setDistinct(true)
        .fetch("billingAddress","city")
        .setMaxRows(100);

    List<String> cities = query.findSingleAttributeList();

    assertThat(sqlOf(query)).contains("select distinct t1.city from o_customer t0 left join o_address t1 on t1.id = t0.billing_address_id");
    assertThat(cities).contains("Auckland").containsNull();
  }

  @Test
  public void distinctSelectOnInheritedBean() {

    ResetBasicData.reset();

    Query<ChildA> query = Ebean.find(ChildA.class)
        .setDistinct(true)
        .select("more")
        .setMaxRows(100);

    query.findSingleAttributeList();
    assertThat(sqlOf(query)).contains("select distinct t0.more from rawinherit_parent t0 where t0.type = 'A'  limit 100");

  }

  @Test
  public void distinctFetchManyToOneInheritedBean() {

    ResetBasicData.reset();

    Query<EUncle> query = Ebean.find(EUncle.class)
        .setDistinct(true)
        .fetch("parent","more")
        .setMaxRows(100);

    query.findSingleAttributeList();

    assertThat(sqlOf(query)).contains("select distinct t1.more from rawinherit_uncle t0 join rawinherit_parent t1 on t1.id = t0.parent_id and t1.type in ('A','B')");

  }

  // hmm - same problem when not using distinct
  @Test
  public void findSingleOnIdProperty(){
    Query<Customer> query = Ebean.find(Customer.class)
        .select("id")
        .setMaxRows(100);

    List<String> ids = query.findSingleAttributeList();
    if (isSqlServer()) {
      assertThat(sqlOf(query)).contains("select top 100 t0.id from o_customer t0");
    } else {
      assertThat(sqlOf(query)).contains("select t0.id from o_customer t0 limit 100");
    }
    assertThat(ids).isNotEmpty();
  }

  @Test
  public void findSingleWithFetch() {

    ResetBasicData.reset();

    Query<Customer> query = Ebean.find(Customer.class)
        .fetch("billingAddress","city")
        .setMaxRows(100);

    List<String> cities = query.findSingleAttributeList();

    assertThat(cities).contains("Auckland").containsNull();
    assertThat(sqlOf(query)).contains("select t1.city from o_customer t0 left join o_address t1 on t1.id = t0.billing_address_id");
  }

  @Test
  public void findSingleSelectOnInheritedBean() {

    ResetBasicData.reset();

    Query<ChildA> query = Ebean.find(ChildA.class)
        .select("more")
        .setMaxRows(100);

    query.findSingleAttributeList();
    assertThat(sqlOf(query)).contains("select t0.more from rawinherit_parent t0 where t0.type = 'A'  limit 100");

  }

  @Test
  public void findSingleFetchManyToOneInheritedBean() {

    ResetBasicData.reset();

    Query<EUncle> query = Ebean.find(EUncle.class)
        .fetch("parent","more")
        .setMaxRows(100);

    query.findSingleAttributeList();

    assertThat(sqlOf(query)).contains("select t1.more from rawinherit_uncle t0 join rawinherit_parent t1 on t1.id = t0.parent_id and t1.type in ('A','B')");

  }

  @Test
  @Ignore //don't know if ebean can handle this on many to many, as this means that the cartesian product is generated
  
  public void distinctFetchManyToManyInheritedBean() {

    ResetBasicData.reset();

    Query<Data> query = Ebean.find(Data.class)
        .setDistinct(true)
        .fetch("parents","more")
        .setMaxRows(100);

    query.findSingleAttributeList();

    assertThat(sqlOf(query)).contains("select distinct t0.more from rawinherit_data t0 " 
        + "join rawinherit_parent_rawinherit_data t1 on t0.id = t1.rawinherit_data_id "
        + "join parent t2 on t1.rawinherit_parent_id = t2.id");
  }
}
