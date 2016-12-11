package org.tests.query.joins;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.tests.model.basic.MRole;
import org.tests.model.basic.MUser;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestDisjunctWhereOuterJoin extends BaseTestCase {

  @Test
  public void test() {

    Ebean.beginTransaction();
    try {

      MRole r1 = new MRole();
      r1.setRoleName("role1B");
      Ebean.save(r1);

      MRole r2 = new MRole();
      r2.setRoleName("role2specialB");
      Ebean.save(r2);

      MRole r3 = new MRole();
      r3.setRoleName("role3B");
      Ebean.save(r3);

      MUser u0 = new MUser();
      u0.setUserName("user0B");
      u0.addRole(r1);
      u0.addRole(r2);

      Ebean.save(u0);

      MUser u1 = new MUser();
      u1.setUserName("user1B");
      u1.addRole(r1);

      Ebean.save(u1);


      Query<MUser> query = Ebean.find(MUser.class)
        .where().disjunction()
        .eq("roles.roleName", "role2specialB") // user0
        .eq("roles.roleName", "role3B") // nobody
        .endJunction().query();

      List<MUser> list = query.findList();
      Assert.assertSame(1, list.size()); // list should contain user0
      System.out.println(list);

      String sql = query.getGeneratedSql();
      Assert.assertTrue(sql.contains("select distinct"));
      Assert.assertTrue(sql.contains("left join mrole "));
      Assert.assertTrue(sql.contains(".role_name = ?"));

    } finally {
      Ebean.rollbackTransaction();
    }

  }
}
