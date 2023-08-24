package org.tests.delete;

import io.ebean.*;
import io.ebean.xtest.BaseTestCase;
import io.ebean.xtest.IgnorePlatform;
import io.ebean.annotation.Platform;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDeleteByQuery extends BaseTestCase {

  private void createUser(String name) {
    BBookmarkUser u1 = new BBookmarkUser(name);
    DB.save(u1);
  }

  @Test
  void maxDelete_expect_singleDeleteStatement() {
    LoggedSql.start();
    DB.find(BBookmarkUser.class)
      .where().eq("name", "a")
      .setMaxRows(10)
      .delete();

    DB.find(BBookmarkUser.class)
      .where().eq("name", "b")
      .setMaxRows(20)
      .delete();

    List<String> loggedSql = LoggedSql.stop();
    assertThat(loggedSql).hasSize(2);
    if (isH2()) {
      assertThat(loggedSql.get(0)).contains("delete from bbookmark_user where id in (select t0.id from bbookmark_user t0 where t0.name = ? limit 10)");
      assertThat(loggedSql.get(1)).contains("delete from bbookmark_user where id in (select t0.id from bbookmark_user t0 where t0.name = ? limit 20)");
    }
  }

  @Test
  void maxDelete_when_beanCaching_expect_selectThenDelete() {
    var a0 = new Article("deleteMe1", "auth1");
    var a1 = new Article("deleteMe2", "auth1");
    DB.saveAll(a0, a1);

    LoggedSql.start();
    DB.find(Article.class)
      .where().eq("name", "deleteMe1")
      .setMaxRows(10)
      .delete();

    DB.find(Article.class)
      .where().eq("name", "deleteMe2")
      .setMaxRows(20)
      .delete();

    List<String> loggedSql = LoggedSql.stop();
    assertThat(loggedSql).hasSize(6);
    if (isH2()) {
      assertThat(loggedSql.get(0)).contains("select t0.id from article t0 where t0.name = ? limit 10");
      assertThat(loggedSql.get(3)).contains("select t0.id from article t0 where t0.name = ? limit 20");
    }
  }

  @Test
  @IgnorePlatform({Platform.MYSQL, Platform.MARIADB})
  public void deleteWithLimit() {
    createUser("deleteWithLimit1");
    createUser("deleteWithLimit2");
    createUser("deleteWithLimit3");
    createUser("deleteWithLimit4");

    LoggedSql.start();
    final int rows = DB.find(BBookmarkUser.class)
      .where().startsWith("name", "deleteWithLimit")
      .setMaxRows(3)
      .delete();

    assertThat(rows).isEqualTo(3);

    List<String> sql = LoggedSql.stop();
    if (isSqlServer()) {
      assertSql(sql.get(0)).contains("delete from bbookmark_user where id in (select top 3 t0.id from bbookmark_user t0 where t0.name like ");
    } else {
      assertSql(sql.get(0)).contains("delete from bbookmark_user where id in (select t0.id from bbookmark_user t0 where t0.name like");
      if (isH2() || isPostgresCompatible()) {
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

    LoggedSql.start();
    query.delete();

    List<String> loggedSql = LoggedSql.stop();
    assertThat(loggedSql).hasSize(1);
    assertThat(trimSql(loggedSql.get(0), 1)).contains("delete from bbookmark_user where id in (select t0.id from bbookmark_user t0 left join bbookmark_org t1 on t1.id = t0.org_id where t1.name");

    Query<BBookmarkUser> query2 = server.find(BBookmarkUser.class)
      .where().eq("name", "NotARealFirstName").query();

    LoggedSql.start();
    query2.delete();

    loggedSql = LoggedSql.stop();
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

    LoggedSql.start();
    query.delete();

    List<String> loggedSql = LoggedSql.stop();
    assertThat(loggedSql).hasSize(1);
    assertThat(trimSql(loggedSql.get(0), 1)).contains("select t0.id from contact t0 left join contact_group t1 on t1.id = t0.group_id where t1.name = ?");

    Query<Contact> query2 = server.find(Contact.class).where().eq("firstName", "NotARealFirstName").query();

    LoggedSql.start();
    query2.delete();

    loggedSql = LoggedSql.stop();
    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).contains("select t0.id from contact t0 where t0.first_name = ?");


    server.find(Contact.class).select("id").where().eq("firstName", "NotARealFirstName").delete();
    server.find(Contact.class).select("id").where().eq("firstName", "TwoAlsoNotRealFirstName").query().delete();

    List<Contact> list = server.find(Contact.class).select("id").where().eq("firstName", "NotARealFirstName").findList();
    assertThat(list).isEmpty();
  }

  @Test
  public void queryByIdDelete() {

    LoggedSql.start();

    DB.find(BBookmarkUser.class).where().eq("id", 7000).delete();
    DB.find(BBookmarkUser.class).setId(7000).delete();

    List<String> sql = LoggedSql.stop();
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

    LoggedSql.start();

    DB.find(Contact.class).where().eq("id", 7000).delete();
    DB.find(Contact.class).setId(7000).delete();

    List<String> sql = LoggedSql.stop();
    // escalate to fetch ids then delete ... but no rows found
    assertSql(sql.get(0)).contains("select t0.id from contact t0 where t0.id = ?");
    assertSql(sql.get(1)).contains("select t0.id from contact t0 where t0.id = ?");

    // and note this is the easiest option
    DB.delete(Contact.class, 7000);
  }

  @Test
  public void queryDelete_withTransactionNoCascade() {

    LoggedSql.start();

    try (Transaction transaction = DB.beginTransaction()) {
      transaction.setPersistCascade(false);

      DB.find(Contact.class).where().eq("id", 7001).delete();

      transaction.commit();
    }

    List<String> sql = LoggedSql.stop();
    if (isPlatformSupportsDeleteTableAlias()) {
      assertSql(sql.get(0)).contains("delete from contact t0 where t0.id = ?");
    } else if (!isMySql()){
      assertSql(sql.get(0)).contains("delete from contact where id = ?");
    }
  }

  @Test
  public void testWithForUpdate() {

    LoggedSql.start();

    DB.find(Customer.class)
      .where().eq("name", "Don Roberto")
      .query().forUpdate()
      .delete();

    List<String> sql = LoggedSql.stop();
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

    // check if we can delete more than 2100 in a batch on SqlServer
    List<Contact> moreContacts = new ArrayList<>();
    for (int i = 0; i < 2200; i++) {
      Contact c = new Contact();
      c.setFirstName("DelByQueryFirstName");
      c.setLastName("deleteMe");
      c.setCustomer(all.get(0));
      moreContacts.add(c);
    }
    DB.saveAll(moreContacts);

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
    //DB.getDefault().getPluginApi().getBeanType(Country.class).clearQueryCache();
    assertThat(query.findList()).hasSize(0);
    assertThat(query.findCount()).isEqualTo(0);
  }
}
