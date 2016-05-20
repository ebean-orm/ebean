package com.avaje.tests.delete;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Contact;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;
import org.avaje.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDeleteByQuery extends BaseTestCase {

  @Test
  public void test() {

    EbeanServer server = Ebean.getDefaultServer();
    if (server.getName().equals("mysql")) {
      // MySql does not the sub query selecting from the delete table
      return;
    }

    Query<Contact> query = server.find(Contact.class).where().eq("group.name", "NahYeahMaybe").query();

    LoggedSqlCollector.start();
    server.delete(query, null);

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).contains("delete from contact where id in (select t0.id c0 from contact t0 left outer join");

    Query<Contact> query2 = server.find(Contact.class).where().eq("firstName", "NotARealFirstName").query();

    LoggedSqlCollector.start();
    server.delete(query2, null);

    loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).contains("delete from contact where first_name =");


    server.find(Contact.class).select("id").where().eq("firstName", "NotARealFirstName").delete();
    server.find(Contact.class).select("id").where().eq("firstName", "TwoAlsoNotRealFirstName").query().delete();

    List<Contact> list = server.find(Contact.class).select("id").where().eq("firstName", "NotARealFirstName").findList();
    assertThat(list).isEmpty();
  }

  @Test
  public void testWithForUpdate() {

    LoggedSqlCollector.start();

    Ebean.find(Customer.class)
        .where().eq("name","Don Roberto")
        .query().setForUpdate(true)
        .delete();

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("delete from o_customer where name = ?");
  }

  @Test
  public void testCommit() {

    ResetBasicData.reset();

    List<Customer> all = Customer.find.all();
    Contact contact = new Contact();
    contact.setFirstName("DelByQueryFirstName");
    contact.setLastName("deleteMe");
    contact.setCustomer(all.get(0));

    Ebean.save(contact);

    Ebean.find(Contact.class).where().eq("firstName", "DelByQueryFirstName").delete();

    Contact contactFind = Ebean.find(Contact.class, contact.getId());
    assertThat(contactFind).isNull();
  }
}
