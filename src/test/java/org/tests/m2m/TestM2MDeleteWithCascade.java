package org.tests.m2m;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.MRole;
import org.tests.model.basic.MUser;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestM2MDeleteWithCascade extends BaseTestCase {

  @Test
  public void test() {

    MRole r0 = new MRole("r0");
    MRole r1 = new MRole("r1");

    Ebean.save(r0);
    // Ebean.save(r1);

    MUser u0 = new MUser("usr0");
    u0.addRole(r0);
    u0.addRole(r1);

    Ebean.save(u0);

    List<MRole> roles = u0.getRoles();

    // this will delete
    Ebean.delete(u0);

    MUser notThere = Ebean.find(MUser.class, u0.getUserid());
    Assert.assertNull("been deleted", notThere);

    List<Object> roleIds = new ArrayList<>();
    Collections.addAll(roleIds, r0.getRoleid(), r1.getRoleid());

    int rc = Ebean.find(MRole.class).where().idIn(roleIds).findCount();

    Assert.assertEquals("roles not deleted", 2, rc);

    Ebean.deleteAll(roles);

    rc = Ebean.find(MRole.class).where().idIn(roleIds).findCount();

    Assert.assertEquals("roles deleted now", 0, rc);
  }
}
