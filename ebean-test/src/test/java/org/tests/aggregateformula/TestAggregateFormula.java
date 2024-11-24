package org.tests.aggregateformula;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.xtest.IgnorePlatform;
import io.ebean.annotation.Platform;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestAggregateFormula extends BaseTestCase {

  @IgnorePlatform({Platform.SQLSERVER, Platform.MYSQL, Platform.MARIADB, Platform.NUODB, Platform.COCKROACH})
  @Test
  public void minDistinctOrderByNulls() {

    ResetBasicData.reset();

    LoggedSql.start();

    List<Contact> contacts = DB.find(Contact.class)
      .setDistinct(true)
      .select("lastName, min(customer)")
      .orderBy("min(customer) asc nulls last")
      .findList();

    List<String> sql = LoggedSql.stop();
    assertSql(sql.get(0)).contains("select distinct t0.last_name, min(t0.customer_id) from contact t0 group by t0.last_name order by min(t0.customer_id) nulls last");

    assertThat(contacts).isNotEmpty();

    Contact contact = contacts.get(0);
    assertThat(contact.getLastName()).isNotNull();
    assertThat(contact.getCustomer()).isNotNull();
    assertThat(contact.getCustomer().getId()).isNotNull();
  }

  @Test
  public void minOnManyToOne() {

    ResetBasicData.reset();

    LoggedSql.start();

    List<Contact> contacts = DB.find(Contact.class)
      .select("lastName, min(customer)")
      .findList();

    List<String> sql = LoggedSql.stop();
    assertSql(sql.get(0)).contains("select t0.last_name, min(t0.customer_id) from contact t0 group by t0.last_name");

    assertThat(contacts).isNotEmpty();

    Contact contact = contacts.get(0);
    assertThat(contact.getLastName()).isNotNull();
    assertThat(contact.getCustomer()).isNotNull();
    assertThat(contact.getCustomer().getId()).isNotNull();
  }

  @Test
  public void minOnManyToOne_withFetchQuery() {

    ResetBasicData.reset();

    LoggedSql.start();

    List<Contact> contacts = DB.find(Contact.class)
      .select("lastName, min(customer)")
      .fetchQuery("customer", "name, status")
      .findList();

    for (Contact contact : contacts) {
      Customer customer = contact.getCustomer();
      assertNotNull(customer.getName());
    }

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(2);
    assertSql(sql.get(0)).contains("select t0.last_name, min(t0.customer_id) from contact t0 group by t0.last_name");
    assertSql(sql.get(1)).contains("select t0.id, t0.name, t0.status from o_customer t0 where");

    assertThat(contacts).isNotEmpty();

    Contact contact = contacts.get(0);
    assertThat(contact.getLastName()).isNotNull();
    assertThat(contact.getCustomer()).isNotNull();
    assertThat(contact.getCustomer().getId()).isNotNull();
  }

  @Test
  public void minOnManyToOne_withFetchQueryWithJoin() {

    ResetBasicData.reset();
    LoggedSql.start();

    List<Contact> contacts = DB.find(Contact.class)
      .select("lastName, min(customer)")
      .fetchQuery("customer", "name, status")
      .fetch("customer.billingAddress", "city, country")
      .findList();

    for (Contact contact : contacts) {
      Customer customer = contact.getCustomer();
      assertNotNull(customer.getName());
      final Address billingAddress = customer.getBillingAddress();
      if (billingAddress != null) {
        assertNotNull(billingAddress.getCountry());
      }
    }

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(2);
    assertSql(sql.get(0)).contains("select t0.last_name, min(t0.customer_id) from contact t0 group by t0.last_name");
    assertSql(sql.get(1)).contains("select t0.id, t0.name, t0.status, t1.id, t1.city, t1.country_code from o_customer t0 left join o_address t1 on t1.id = t0.billing_address_id where");

    assertThat(contacts).isNotEmpty();

    Contact contact = contacts.get(0);
    assertThat(contact.getLastName()).isNotNull();
    assertThat(contact.getCustomer()).isNotNull();
    assertThat(contact.getCustomer().getId()).isNotNull();
  }

  @Test
  public void sum_withoutAlias() {

    ResetBasicData.reset();

    LoggedSql.start();

    List<Order> orders = DB.find(Order.class)
      .select("status, sum(id)")
      .findList();

    List<String> sql = LoggedSql.stop();
    assertSql(sql.get(0)).contains("select t0.status, sum(t0.id) from o_order t0 group by t0.status");

    assertThat(orders).isNotEmpty();
    for (Order order : orders) {
      assertThat(order.getStatus()).isNotNull();
      assertThat(order.getId()).isNotNull();
    }
  }

  @Test
  public void sum_withAlias() {

    ResetBasicData.reset();

    LoggedSql.start();

    List<Order> orders = DB.find(Order.class)
      .select("status, sum(id) as id")
      .findList();

    List<String> sql = LoggedSql.stop();
    assertSql(sql.get(0)).contains("select t0.status, sum(t0.id) id from o_order t0 group by t0.status");

    assertThat(orders).isNotEmpty();
    for (Order order : orders) {
      assertThat(order.getStatus()).isNotNull();
      assertThat(order.getId()).isNotNull();
    }
  }

  @Test
  public void sumWithManyToOne_min() {

    ResetBasicData.reset();

    LoggedSql.start();

    List<Contact> contacts = DB.find(Contact.class)
      .select("customer, min(cretime)")
      .findList();

    List<String> sql = LoggedSql.stop();
    assertSql(sql.get(0)).contains("select t0.customer_id, min(t0.cretime) from contact t0 group by t0.customer_id");

    assertThat(contacts).isNotEmpty();
  }

  @Test
  public void sumWithManyToOne_minMax() {

    ResetBasicData.reset();

    LoggedSql.start();

    List<Contact> contacts = DB.find(Contact.class)
      .select("customer, min(cretime), max(updtime)")
      .findList();

    List<String> sql = LoggedSql.stop();
    assertSql(sql.get(0)).contains("select t0.customer_id, min(t0.cretime), max(t0.updtime) from contact t0 group by t0.customer_id");

    assertThat(contacts).isNotEmpty();
  }


}
