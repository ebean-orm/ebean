package org.tests.model.onetoone;

import io.ebean.BaseTestCase;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestOneToOneOptionalRelationship extends BaseTestCase {

  @Test
  public void test() {

    Account account = new Account();
    account.setName("AC234");
    account.save();

    LoggedSqlCollector.start();

    Account fetchedAccount = Account.find.byId(account.getId());
    Assert.assertNotNull(fetchedAccount);

    List<String> loggedSql = LoggedSqlCollector.stop();
    Assert.assertEquals(1, loggedSql.size());

    // select t0.id c0, t0.name c1, t0.version c2, t0.when_created c3, t0.when_updated c4, t1.id c5
    // from oto_account t0
    // join oto_user t1 on t1.account_id = t0.id
    // where t0.id = ?

    String sql = trimSql(loggedSql.get(0), 1);
    Assert.assertTrue(sql.contains("select t0.id, t0.name"));
    Assert.assertTrue(sql.contains(" from oto_account t0 left join oto_user t1 on t1.account_id = t0.id  where t0.id = ?"));
  }


  @Test
  public void testWithUser() {

    Account account = new Account();
    account.setName("AC678");
    account.save();

    User user = new User();
    user.setName("Geoff");
    user.setAccount(account);
    user.save();

    LoggedSqlCollector.start();

    Account fetchedAccount = Account.find.byId(account.getId());
    Assert.assertNotNull(fetchedAccount);

    Assert.assertNotNull(fetchedAccount.getUser());
    Assert.assertEquals(user.getId(), fetchedAccount.getUser().getId());
    Assert.assertEquals(user.getName(), fetchedAccount.getUser().getName());

    List<String> loggedSql = LoggedSqlCollector.stop();
    Assert.assertEquals(2, loggedSql.size());

    // select t0.id c0, t0.name c1, t0.version c2, t0.when_created c3, t0.when_updated c4, t1.id c5
    // from oto_account t0
    // join oto_user t1 on t1.account_id = t0.id
    // where t0.id = ?

    String sql = trimSql(loggedSql.get(0), 1);
    Assert.assertTrue(sql.contains("select t0.id, t0.name"));
    Assert.assertTrue(sql.contains(" from oto_account t0 left join oto_user t1 on t1.account_id = t0.id  where t0.id = ?"));

    String lazyLoadSql = trimSql(loggedSql.get(1), 5);
    Assert.assertTrue(lazyLoadSql.contains("select t0.id, t0.name, t0.version, t0.when_created, t0.when_modified, t0.account_id from oto_user t0 where t0.id = ?"));
  }


  @Test
  public void testWithUserFetch() {

    Account account = new Account();
    account.setName("AC786");
    account.save();

    User user = new User();
    user.setName("Jane");
    user.setAccount(account);
    user.save();

    LoggedSqlCollector.start();

    Account fetchedAccount = Account.find.query().fetch("user").setId(account.getId()).findOne();
    Assert.assertNotNull(fetchedAccount);

    Assert.assertNotNull(fetchedAccount.getUser());
    Assert.assertEquals(user.getId(), fetchedAccount.getUser().getId());
    Assert.assertEquals(user.getName(), fetchedAccount.getUser().getName());

    List<String> loggedSql = LoggedSqlCollector.stop();
    Assert.assertEquals(1, loggedSql.size());

    // select t0.id c0, t0.name c1, t0.version c2, t0.when_created c3, t0.when_updated c4, t1.id c5
    // from oto_account t0
    // join oto_user t1 on t1.account_id = t0.id
    // where t0.id = ?

    String sql = trimSql(loggedSql.get(0), 1);
    Assert.assertTrue(sql.contains("select t0.id, t0.name"));
    Assert.assertTrue(sql.contains(" from oto_account t0 left join oto_user t1 on t1.account_id = t0.id  where t0.id = ?"));
  }
}
