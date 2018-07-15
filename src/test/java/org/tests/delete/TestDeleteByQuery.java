package org.tests.delete;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Query;
import io.ebean.annotation.IgnorePlatform;
import io.ebean.annotation.Platform;

import org.tests.model.basic.BBookmarkUser;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Country;
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
  public void deleteWithSubquery() {

    EbeanServer server = Ebean.getDefaultServer();

    BBookmarkUser u1 = new BBookmarkUser("u1");
    Ebean.save(u1);

    Query<BBookmarkUser> query = server.find(BBookmarkUser.class)
      .where().eq("org.name", "NahYeahMaybe")
      .query();

    LoggedSqlCollector.start();
    query.delete();

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(1);
    assertThat(trimSql(loggedSql.get(0), 1)).contains("delete from bbookmark_user where id in (select t0.id from bbookmark_user t0 left join bbookmark_org t1 on t1.id = t0.org_id  where t1.name");

    Query<BBookmarkUser> query2 = server.find(BBookmarkUser.class)
      .where().eq("name", "NotARealFirstName").query();

    LoggedSqlCollector.start();
    query2.delete();

    loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).contains("delete from bbookmark_user where name =");


    server.find(BBookmarkUser.class).select("id").where().eq("name", "NotARealFirstName").delete();
    server.find(BBookmarkUser.class).select("id").where().eq("name", "TwoAlsoNotRealFirstName").query().delete();

    List<BBookmarkUser> list = server.find(BBookmarkUser.class).select("id").where().eq("name", "NotARealFirstName").findList();
    assertThat(list).isEmpty();
  }

  @Test
  @IgnorePlatform(Platform.MYSQL)
  // FIXME: MySql does not the sub query selecting from the delete table
  public void deleteWithSubquery_withEscalation() {

    EbeanServer server = Ebean.getDefaultServer();

    Query<Contact> query = server.find(Contact.class).where().eq("group.name", "NahYeahMaybe").query();

    LoggedSqlCollector.start();
    query.delete();

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(1);
    assertThat(trimSql(loggedSql.get(0), 1)).contains("select t0.id from contact t0 left join contact_group t1 on t1.id = t0.group_id  where t1.name = ?");

    Query<Contact> query2 = server.find(Contact.class).where().eq("firstName", "NotARealFirstName").query();

    LoggedSqlCollector.start();
    query2.delete();

    loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).contains("select t0.id from contact t0 where t0.first_name = ?");


    server.find(Contact.class).select("id").where().eq("firstName", "NotARealFirstName").delete();
    server.find(Contact.class).select("id").where().eq("firstName", "TwoAlsoNotRealFirstName").query().delete();

    List<Contact> list = server.find(Contact.class).select("id").where().eq("firstName", "NotARealFirstName").findList();
    assertThat(list).isEmpty();
  }

  @Test
  public void queryByIdDelete() {

    LoggedSqlCollector.start();

    Ebean.find(BBookmarkUser.class).where().eq("id", 7000).delete();
    Ebean.find(BBookmarkUser.class).setId(7000).delete();

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql.get(0)).contains("delete from bbookmark_user where id = ?");
    assertThat(sql.get(1)).contains("delete from bbookmark_user where id = ?");

    // and note this is the easiest option
    Ebean.delete(BBookmarkUser.class, 7000);
  }

  @Test
  public void queryByIdDelete_withEscalation() {

    LoggedSqlCollector.start();

    Ebean.find(Contact.class).where().eq("id", 7000).delete();
    Ebean.find(Contact.class).setId(7000).delete();

    List<String> sql = LoggedSqlCollector.stop();
    // escalate to fetch ids then delete ... but no rows found
    assertThat(sql.get(0)).contains("select t0.id from contact t0 where t0.id = ?");
    assertThat(sql.get(1)).contains("select t0.id from contact t0 where t0.id = ?");

    // and note this is the easiest option
    Ebean.delete(Contact.class, 7000);
  }

  @Test
  public void testWithForUpdate() {

    LoggedSqlCollector.start();

    Ebean.find(Customer.class)
      .where().eq("name", "Don Roberto")
      .query().forUpdate()
      .delete();

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("select t0.id from o_customer t0 where t0.name = ?");
  }

  @Test
  public void deleteByPredicate() {

    BBookmarkUser ud = new BBookmarkUser("deleteQueryByPredicate");
    Ebean.save(ud);

    Ebean.find(BBookmarkUser.class).where().eq("name", "deleteQueryByPredicate").delete();

    BBookmarkUser found = Ebean.find(BBookmarkUser.class, ud.getId());
    assertThat(found).isNull();
  }

  @Test
  public void deleteByPredicate_withEscalation() {

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

  @Test
  public void deleteByPredicateCached() {

    Country country = new Country();
    country.setCode("XX");
    country.setName("SecretName");
    Ebean.save(country);
    Query<Country> query = Ebean.find(Country.class).where().eq("name", "SecretName").setUseQueryCache(true);

    assertThat(query.findList()).hasSize(1);
    assertThat(query.findCount()).isEqualTo(1);

    Ebean.find(Country.class).where().eq("name", "SecretName").delete();
    //Ebean.getDefaultServer().getPluginApi().getBeanType(Country.class).clearQueryCache();
    assertThat(query.findList()).hasSize(0);
    assertThat(query.findCount()).isEqualTo(0);
  }
}
