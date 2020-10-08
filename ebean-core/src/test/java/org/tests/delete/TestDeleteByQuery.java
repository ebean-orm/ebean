package org.tests.delete;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.Query;
import io.ebean.Transaction;
import io.ebean.annotation.IgnorePlatform;
import io.ebean.annotation.Platform;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;
import org.tests.model.basic.BBookmarkUser;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Country;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDeleteByQuery extends BaseTestCase {

  private void createUser(String name) {
    BBookmarkUser u1 = new BBookmarkUser(name);
    DB.save(u1);
  }

  @Test
  @IgnorePlatform({Platform.MYSQL, Platform.MARIADB})
  public void deleteWithLimit() {
    createUser("deleteWithLimit1");
    createUser("deleteWithLimit2");
    createUser("deleteWithLimit3");
    createUser("deleteWithLimit4");

    LoggedSqlCollector.start();
    final int rows = DB.find(BBookmarkUser.class)
      .where().startsWith("name", "deleteWithLimit")
      .setMaxRows(3)
      .delete();

    assertThat(rows).isEqualTo(3);

    List<String> sql = LoggedSqlCollector.stop();
    if (isSqlServer()) {
      assertSql(sql.get(0)).contains("delete from bbookmark_user where id in (select top 3 t0.id from bbookmark_user t0 where t0.name like ");
    } else {
      assertSql(sql.get(0)).contains("delete from bbookmark_user where id in (select t0.id from bbookmark_user t0 where t0.name like");
      if (isH2() || isPostgres()) {
        assertSql(sql.get(0)).contains("limit 3");
      }
    }

    final int rowsAfter = DB.find(BBookmarkUser.class)
      .where().startsWith("name", "deleteWithLimit")
      .setMaxRows(3)
      .delete();

    assertThat(rowsAfter).isEqualTo(1);
  }

  @Test
  @IgnorePlatform(Platform.MYSQL)
  // FIXME: MySql does not the sub query selecting from the delete table
  public void deleteWithSubquery() {

    Database server = DB.getDefault();

    BBookmarkUser u1 = new BBookmarkUser("u1");
    DB.save(u1);

    Query<BBookmarkUser> query = server.find(BBookmarkUser.class)
      .where().eq("org.name", "NahYeahMaybe")
      .query();

    LoggedSqlCollector.start();
    query.delete();

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(1);
    assertThat(trimSql(loggedSql.get(0), 1)).contains("delete from bbookmark_user where id in (select t0.id from bbookmark_user t0 left join bbookmark_org t1 on t1.id = t0.org_id where t1.name");

    Query<BBookmarkUser> query2 = server.find(BBookmarkUser.class)
      .where().eq("name", "NotARealFirstName").query();

    LoggedSqlCollector.start();
    query2.delete();

    loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(1);
    if (isPlatformSupportsDeleteTableAlias()) {
      assertThat(loggedSql.get(0)).contains("delete from bbookmark_user t0 where t0.name =");
    } else if (isMySql()){
      assertThat(loggedSql.get(0)).contains("delete t0 from bbookmark_user t0 where t0.name =");
    } else {
      assertThat(loggedSql.get(0)).contains("delete from bbookmark_user where name =");
    }


    server.find(BBookmarkUser.class).select("id").where().eq("name", "NotARealFirstName").delete();
    server.find(BBookmarkUser.class).select("id").where().eq("name", "TwoAlsoNotRealFirstName").query().delete();

    List<BBookmarkUser> list = server.find(BBookmarkUser.class).select("id").where().eq("name", "NotARealFirstName").findList();
    assertThat(list).isEmpty();
  }

  @Test
  @IgnorePlatform(Platform.MYSQL)
  // FIXME: MySql does not the sub query selecting from the delete table
  public void deleteWithSubquery_withEscalation() {

    Database server = DB.getDefault();

    Query<Contact> query = server.find(Contact.class).where().eq("group.name", "NahYeahMaybe").query();

    LoggedSqlCollector.start();
    query.delete();

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(1);
    assertThat(trimSql(loggedSql.get(0), 1)).contains("select t0.id from contact t0 left join contact_group t1 on t1.id = t0.group_id where t1.name = ?");

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

    DB.find(BBookmarkUser.class).where().eq("id", 7000).delete();
    DB.find(BBookmarkUser.class).setId(7000).delete();

    List<String> sql = LoggedSqlCollector.stop();
    if (isPlatformSupportsDeleteTableAlias()) {
      assertSql(sql.get(0)).contains("delete from bbookmark_user t0 where t0.id = ?");
      assertSql(sql.get(1)).contains("delete from bbookmark_user t0 where t0.id = ?");
    } else if (!isMySql()) {
      assertSql(sql.get(0)).contains("delete from bbookmark_user where id = ?");
      assertSql(sql.get(1)).contains("delete from bbookmark_user where id = ?");
    }

    // and note this is the easiest option
    DB.delete(BBookmarkUser.class, 7000);
  }

  @Test
  public void queryByIdDelete_withEscalation() {

    LoggedSqlCollector.start();

    DB.find(Contact.class).where().eq("id", 7000).delete();
    DB.find(Contact.class).setId(7000).delete();

    List<String> sql = LoggedSqlCollector.stop();
    // escalate to fetch ids then delete ... but no rows found
    assertSql(sql.get(0)).contains("select t0.id from contact t0 where t0.id = ?");
    assertSql(sql.get(1)).contains("select t0.id from contact t0 where t0.id = ?");

    // and note this is the easiest option
    DB.delete(Contact.class, 7000);
  }

  @Test
  public void queryDelete_withTransactionNoCascade() {

    LoggedSqlCollector.start();

    try (Transaction transaction = DB.beginTransaction()) {
      transaction.setPersistCascade(false);

      DB.find(Contact.class).where().eq("id", 7001).delete();

      transaction.commit();
    }

    List<String> sql = LoggedSqlCollector.stop();
    if (isPlatformSupportsDeleteTableAlias()) {
      assertSql(sql.get(0)).contains("delete from contact t0 where t0.id = ?");
    } else if (!isMySql()){
      assertSql(sql.get(0)).contains("delete from contact where id = ?");
    }
  }

  @Test
  public void testWithForUpdate() {

    LoggedSqlCollector.start();

    DB.find(Customer.class)
      .where().eq("name", "Don Roberto")
      .query().forUpdate()
      .delete();

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(1);
    if (isSqlServer()) {
      assertSql(sql.get(0)).contains("select t0.id from o_customer t0 with (updlock) where t0.name = ?");
    } else {
      assertSql(sql.get(0)).contains("select t0.id from o_customer t0 where t0.name = ?");
    }
  }

  @Test
  public void deleteByPredicate() {

    BBookmarkUser ud = new BBookmarkUser("deleteQueryByPredicate");
    DB.save(ud);

    DB.find(BBookmarkUser.class).where().eq("name", "deleteQueryByPredicate").delete();

    BBookmarkUser found = DB.find(BBookmarkUser.class, ud.getId());
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

    DB.save(contact);

    DB.find(Contact.class).where().eq("firstName", "DelByQueryFirstName").delete();

    Contact contactFind = DB.find(Contact.class, contact.getId());
    assertThat(contactFind).isNull();
  }

  @Test
  public void deleteByPredicateCached() {

    Country country = new Country();
    country.setCode("XX");
    country.setName("SecretName");
    DB.save(country);
    Query<Country> query = DB.find(Country.class).where().eq("name", "SecretName").setUseQueryCache(true);

    assertThat(query.findList()).hasSize(1);
    assertThat(query.findCount()).isEqualTo(1);

    DB.find(Country.class).where().eq("name", "SecretName").delete();
    //Ebean.getDefaultServer().getPluginApi().getBeanType(Country.class).clearQueryCache();
    assertThat(query.findList()).hasSize(0);
    assertThat(query.findCount()).isEqualTo(0);
  }
}
