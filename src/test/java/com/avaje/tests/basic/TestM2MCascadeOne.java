package com.avaje.tests.basic;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.MRole;
import com.avaje.tests.model.basic.MUser;
import org.junit.Test;

public class TestM2MCascadeOne extends BaseTestCase {

  @Test
  public void test() {

    MUser u = new MUser();
    u.setUserName("testM2M");

    u.getRoles();
    Ebean.save(u);

    MRole r0 = new MRole();
    r0.setRoleName("rol_0");
    Ebean.save(r0);

    MRole r1 = new MRole();
    r1.setRoleName("rol_1");

    MUser u1 = Ebean.find(MUser.class, u.getUserid());

    u1.addRole(r0);
    u1.addRole(r1);

    Ebean.save(u1);

  }

  @Test
  public void testRawPredicate_with_ManyToManyPath() {

    Query<MUser> query = Ebean.find(MUser.class)
      .select("userid")
      .where().raw("roles.roleid in (?)", 24)
      .query();

    query.findList();

  }
}
