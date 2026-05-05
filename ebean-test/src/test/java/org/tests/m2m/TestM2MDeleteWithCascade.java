package org.tests.m2m;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.MRole;
import org.tests.model.basic.MUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestM2MDeleteWithCascade extends BaseTestCase {

  @Test
  public void test() {

    MRole r0 = new MRole("r0");
    MRole r1 = new MRole("r1");

    DB.save(r0);
    // DB.save(r1);

    MUser u0 = new MUser("usr0");
    u0.addRole(r0);
    u0.addRole(r1);

    DB.save(u0);

    List<MRole> roles = u0.getRoles();

    // this will delete
    DB.delete(u0);

    MUser notThere = DB.find(MUser.class, u0.getUserid());
    assertNull(notThere);

    List<Object> roleIds = new ArrayList<>();
    Collections.addAll(roleIds, r0.getRoleId(), r1.getRoleId());

    int rc = DB.find(MRole.class).where().idIn(roleIds).findCount();

    assertEquals(2, rc);

    DB.deleteAll(roles);

    rc = DB.find(MRole.class).where().idIn(roleIds).findCount();

    assertEquals(0, rc);
  }
}
