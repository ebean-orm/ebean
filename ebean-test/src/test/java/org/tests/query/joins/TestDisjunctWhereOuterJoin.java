package org.tests.query.joins;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Expr;
import io.ebean.Query;
import org.junit.jupiter.api.Test;
import org.tests.basic.one2one.Wheel;
import org.tests.model.basic.MRole;
import org.tests.model.basic.MUser;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDisjunctWhereOuterJoin extends BaseTestCase {

  @Test
  public void test() {

    DB.beginTransaction();
    try {

      MRole r1 = new MRole();
      r1.setRoleName("role1B");
      DB.save(r1);

      MRole r2 = new MRole();
      r2.setRoleName("role2specialB");
      DB.save(r2);

      MRole r3 = new MRole();
      r3.setRoleName("role3B");
      DB.save(r3);

      MUser u0 = new MUser();
      u0.setUserName("user0B");
      u0.addRole(r1);
      u0.addRole(r2);

      DB.save(u0);

      MUser u1 = new MUser();
      u1.setUserName("user1B");
      u1.addRole(r1);

      DB.save(u1);

      queryOrExpression(r2.getRoleid());

      queryDisjunction(r2.getRoleid());

      Query<MUser> query = DB.find(MUser.class)
        .where().disjunction()
        .eq("roles.roleName", "role2specialB") // user0
        .eq("roles.roleName", "role3B") // nobody
        .endJunction().query();

      List<MUser> list = query.findList();
      assertThat(list).hasSize(1); // list should contain user0

      String sql = sqlOf(query);
      assertSqlOuterJoins(sql);
      assertThat(sql).contains(".role_name = ?");

    } finally {
      DB.rollbackTransaction();
    }
  }

  private void queryDisjunction(Integer roleid) {

    Query<MUser> query = DB.find(MUser.class)
      .where().or()
      .eq("userName", "user0B")
      .eq("roles.roleid", roleid)
      .endOr().query();

      query.findList();

    String sql = sqlOf(query);
    assertSqlOuterJoins(sql);
    assertThat(sql).contains("where (t0.user_name = ? or u1.roleid = ?)");
  }

  @Test
  public void testSelectOneToOneDisjunction() {
    DB.beginTransaction();
    try {
      Query<Wheel> query = DB.find(Wheel.class)
              .select("id")
              .where().or()
              .ge("tire.id", 100)
              .lt("tire.id", 100)
              .endOr().query();
      query.findList();
      String sql = sqlOf(query);
      assertThat(sql).contains("join");
    } finally {
      DB.rollbackTransaction();
    }
  }

  private void queryOrExpression(Integer roleid) {

    Query<MUser> query = DB.find(MUser.class)
      .where().or(
        Expr.eq("userName", "user0B"),
        Expr.eq("roles.roleid", roleid)
      )
      .query();

    query.findList();

    String sql = sqlOf(query);
    assertSqlOuterJoins(sql);
    assertThat(sql).contains("where (t0.user_name = ? or u1.roleid = ?)");
  }

  private void assertSqlOuterJoins(String sql) {
    assertThat(sql).contains("select distinct");
    assertThat(sql).contains("left join mrole_muser u1z_ on u1z_.muser_userid = t0.userid");
    assertThat(sql).contains("left join mrole u1 on u1.roleid = u1z_.mrole_roleid");
  }
}
