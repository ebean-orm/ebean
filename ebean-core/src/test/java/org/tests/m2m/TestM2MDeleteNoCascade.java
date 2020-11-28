package org.tests.m2m;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Transaction;
import io.ebean.annotation.IgnorePlatform;
import io.ebean.annotation.Platform;
import org.ebeantest.LoggedSqlCollector;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tests.model.basic.MnocRole;
import org.tests.model.basic.MnocUser;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestM2MDeleteNoCascade extends BaseTestCase {

  private static MnocRole r0 = new MnocRole("r0");
  private static MnocRole r1 = new MnocRole("r1");
  private static MnocRole r2 = new MnocRole("r2");

  @BeforeClass
  public static void setup() {
    DB.save(r0);
    DB.save(r1);
    DB.save(r2);
  }

  @IgnorePlatform(Platform.NUODB)
  @Test
  public void test() {

    MnocUser u0 = new MnocUser("usr0");
    u0.addValidRole(r0);
    u0.addValidRole(r1);

    DB.save(u0);

    MnocUser loadedUser = DB.find(MnocUser.class, u0.getUserId());
    List<MnocRole> validRoles = loadedUser.getValidRoles();
    assertThat(validRoles).hasSize(2);

    LoggedSqlCollector.start();
    DB.delete(u0);

    final List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(2);
    assertSql(sql.get(0)).contains("delete from mnoc_user_mnoc_role where mnoc_user_user_id = ?");
    assertSql(sql.get(1)).contains("delete from mnoc_user where user_id=? and version=?");

    final MnocUser found = DB.find(MnocUser.class, u0.getUserId());
    assertThat(found).isNull();
  }

  @Test
  public void update() {

    MnocUser u0 = new MnocUser("usr1");
    u0.addValidRole(r0);
    u0.addValidRole(r1);

    DB.save(u0);

    List<MnocRole> roles = new ArrayList<>();
    roles.add(new MnocRole(r2));

    final MnocUser user = DB.find(MnocUser.class, u0.getUserId());
    user.setUserName("usr1-mod");
    user.setValidRoles(roles);

    LoggedSqlCollector.start();
    DB.update(user);

    final List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(5);
    assertSql(sql.get(0)).contains("update mnoc_user set user_name=?, version=? where user_id=? and version=?");
    assertSql(sql.get(1)).contains("delete from mnoc_user_mnoc_role where mnoc_user_user_id = ?");
    assertSqlBind(sql.get(2));
    assertThat(sql.get(3)).contains("insert into mnoc_user_mnoc_role (mnoc_user_user_id, mnoc_role_role_id) values (?, ?)");
    assertSqlBind(sql.get(4));
  }

  @Test
  public void deleteBatch() {

    MnocUser u0 = new MnocUser("usr2a");
    u0.addValidRole(r0);
    u0.addValidRole(r1);

    MnocUser u1 = new MnocUser("usr2b");
    u1.addValidRole(r0);
    u1.addValidRole(r1);

    DB.save(u0);
    DB.save(u1);

    LoggedSqlCollector.start();

    try (Transaction txn = DB.beginTransaction()) {
      txn.setBatchMode(true);
      DB.delete(u0);
      DB.delete(u1);
      txn.commit();
    }

    final List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(8);
    assertSql(sql.get(0)).contains("delete from mnoc_user_mnoc_role where mnoc_user_user_id = ?");
    assertThat(sql.get(4)).contains("delete from mnoc_user_mnoc_role where mnoc_user_user_id = ?");
    assertSql(sql.get(2)).contains("delete from mnoc_user where user_id=? and version=?");
    assertThat(sql.get(6)).contains("delete from mnoc_user where user_id=? and version=?");
  }
}
