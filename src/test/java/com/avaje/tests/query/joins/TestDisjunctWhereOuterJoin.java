package com.avaje.tests.query.joins;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.MRole;
import com.avaje.tests.model.basic.MUser;

public class TestDisjunctWhereOuterJoin extends BaseTestCase {

  @Test
  public void test() {

    Ebean.beginTransaction();

    MRole r1 = new MRole();
    r1.setRoleName("role1");
    Ebean.save(r1);

    MRole r2 = new MRole();
    r2.setRoleName("role2special");
    Ebean.save(r2);

    MRole r3 = new MRole();
    r3.setRoleName("role3");
    Ebean.save(r3);

    MUser u0 = new MUser();
    u0.setUserName("user0");
    u0.addRole(r1);
    u0.addRole(r2);

    Ebean.save(u0);

    MUser u1 = new MUser();
    u1.setUserName("user1");
    u1.addRole(r1);

    Ebean.save(u1);

    Ebean.commitTransaction();

    
    Query<MUser> query = Ebean.find(MUser.class)
        .where().disjunction()
                .eq("roles.roleName", "role2special") // user0
                .eq("roles.roleName", "role3") // nobody
            .endJunction().query();

    List<MUser> list = query.findList();
    Assert.assertSame(1, list.size()); // list should contain user0
    System.out.println(list);

    String sql = query.getGeneratedSql();
    Assert.assertTrue(sql.indexOf("select distinct") > -1);
    Assert.assertTrue(sql.indexOf("outer join mrole ") > -1);
    Assert.assertTrue(sql.indexOf(".role_name = ?") > -1);

  }
}
