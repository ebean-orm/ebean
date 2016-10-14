package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Contact;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IsEmptyExpressionQueryTest extends BaseTestCase {

  @Test
  public void isEmpty() {

    ResetBasicData.reset();

    Query<Customer> query = Ebean.find(Customer.class)
        .select("id")
        .where().isEmpty("contacts")
        .query();

    query.findList();
    assertThat(sqlOf(query)).contains("select t0.id from o_customer t0 where not exists (select 1 from contact where customer_id = t0.id");
  }

  @Test
  public void isNotEmpty() {

    ResetBasicData.reset();

    Query<Customer> query = Ebean.find(Customer.class)
        .select("id")
        .where().isNotEmpty("contacts")
        .query();

    query.findList();
    assertThat(sqlOf(query)).contains("select t0.id from o_customer t0 where exists (select 1 from contact where customer_id = t0.id");
  }

  @Test
  public void isEmpty_contacts() {

    ResetBasicData.reset();

    Query<Contact> query = Ebean.find(Contact.class)
        .select("id")
        .where().isEmpty("notes")
        .query();

    query.findList();
    assertThat(sqlOf(query)).contains("select t0.id from contact t0 where not exists (select 1 from contact_note where contact_id = t0.id");
  }

  @Test
  public void isNotEmpty_contacts() {

    ResetBasicData.reset();

    Query<Contact> query = Ebean.find(Contact.class)
        .select("id")
        .where().isNotEmpty("notes")
        .query();

    query.findList();
    assertThat(sqlOf(query)).contains("select t0.id from contact t0 where exists (select 1 from contact_note where contact_id = t0.id");
  }


  @Test
  public void isEmpty_manyToMany() {

    ResetBasicData.reset();

    Query<Contact> query = Ebean.find(Contact.class)
        .select("id")
        .where().isEmpty("notes")
        .query();

    query.findList();
  }


  @Test
  public void isEmpty_nested() {

    ResetBasicData.reset();

    Query<Customer> query = Ebean.find(Customer.class)
        .select("id")
        .where().isEmpty("contacts.notes")
        .query();

    query.findList();
    assertThat(sqlOf(query)).contains("select distinct t0.id from o_customer t0 join contact u1 on u1.customer_id = t0.id  where not exists (select 1 from contact_note where contact_id = u1.id)");
  }

  @Test
  public void isNotEmpty_nested() {

    ResetBasicData.reset();

    Query<Customer> query = Ebean.find(Customer.class)
        .select("id")
        .where().isNotEmpty("contacts.notes")
        .query();

    query.findList();
    assertThat(sqlOf(query)).contains("select distinct t0.id from o_customer t0 join contact u1 on u1.customer_id = t0.id  where exists (select 1 from contact_note where contact_id = u1.id)");
  }

}