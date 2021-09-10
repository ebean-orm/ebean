package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import io.ebean.Transaction;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.MRole;
import org.tests.model.basic.MUser;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestManyWhereJoinM2M extends BaseTestCase {

  @Test
  public void test() {

    try (Transaction txn = DB.beginTransaction()) {

      MRole r1 = new MRole();
      r1.setRoleName("role1");
      DB.save(r1);

      MRole r2 = new MRole();
      r2.setRoleName("role2special");
      DB.save(r2);

      MRole r3 = new MRole();
      r3.setRoleName("role3");
      DB.save(r3);

      MUser u0 = new MUser();
      u0.setUserName("user0");
      u0.addRole(r1);
      u0.addRole(r2);

      DB.save(u0);

      MUser u1 = new MUser();
      u1.setUserName("user1");
      u1.addRole(r1);

      DB.save(u1);

      MUser u2 = new MUser();
      u2.setUserName("user2");
      DB.save(u2);

      txn.commit();
    }
    Query<MUser> query = DB.find(MUser.class).fetch("roles")
      // the where on a 'many' (like orders) requires an
      // additional join and distinct which is independent
      // of a fetch join (if there is a fetch join)
      .where().eq("roles.roleName", "role2special").query();

    query.findList();

    String sql = query.getGeneratedSql();
    assertTrue(sql.contains("select distinct"));
    assertTrue(sql.contains("left join mrole "));
    assertTrue(sql.contains("join mrole "));
    assertTrue(sql.contains(".role_name = ?"));

    isEmpty();
    isNotEmpty();
  }

  private void isEmpty() {

    Query<MUser> query = DB.find(MUser.class)
      .where().isEmpty("roles")
      .query();

    List<MUser> usersWithNoRoles = query.findList();

    assertThat(sqlOf(query, 2)).contains("select t0.userid, t0.user_name, t0.user_type_id from muser t0 where not exists (select 1 from mrole_muser x where x.muser_userid = t0.userid)");
    assertThat(usersWithNoRoles).isNotEmpty();
  }

  private void isNotEmpty() {

    Query<MUser> query = DB.find(MUser.class)
      .select("userName")
      .where().isNotEmpty("roles")
      .query();

    List<MUser> usersWithRoles = query.findList();

    assertThat(sqlOf(query, 1)).contains("select t0.userid, t0.user_name from muser t0 where exists (select 1 from mrole_muser x where x.muser_userid = t0.userid)");
    assertThat(usersWithRoles).isNotEmpty();
  }
}
