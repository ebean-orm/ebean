package org.tests.delete;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Query;
import io.ebean.annotation.IgnorePlatform;
import io.ebean.annotation.Platform;

import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDeleteByQuery extends BaseTestCase {

  @Test
  @IgnorePlatform(Platform.MYSQL)
  // FIXME: MySql does not the sub query selecting from the delete table
  public void test() {

    EbeanServer server = Ebean.getDefaultServer();

    Query<Contact> query = server.find(Contact.class).where().eq("group.name", "NahYeahMaybe").query();

    LoggedSqlCollector.start();
    server.delete(query, null);

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(1);
    assertThat(trimSql(loggedSql.get(0), 1)).contains("delete from contact where id in (select t0.id from contact t0 left join");

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
  public void queryByIdDelete() {

    LoggedSqlCollector.start();

    Ebean.find(Contact.class).where().eq("id", 7000).delete();
    Ebean.find(Contact.class).setId(7000).delete();

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql.get(0)).contains("delete from contact where id = ?");
    assertThat(sql.get(1)).contains("delete from contact where id = ?");

    // and note this is the easiest option
    Ebean.delete(Contact.class, 7000);
  }

  @Test
  public void testWithForUpdate() {

    LoggedSqlCollector.start();

    Ebean.find(Customer.class)
      .where().eq("name", "Don Roberto")
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
