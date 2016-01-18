package com.avaje.tests.query;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.MRole;
import com.avaje.tests.model.basic.MUser;

public class TestManyWhereJoinM2M extends BaseTestCase {

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

    Query<MUser> query = Ebean.find(MUser.class).fetch("roles")
    // the where on a 'many' (like orders) requires an
    // additional join and distinct which is independent
    // of a fetch join (if there is a fetch join)
        .where().eq("roles.roleName", "role2special").query();

    List<MUser> list = query.findList();
    System.out.println(list);

    String sql = query.getGeneratedSql();
    Assert.assertTrue(sql.contains("select distinct"));
    Assert.assertTrue(sql.contains("left outer join mys.mrole "));
    Assert.assertTrue(sql.contains("join mys.mrole "));
    Assert.assertTrue(sql.contains(".role_name = ?"));

  }
}
