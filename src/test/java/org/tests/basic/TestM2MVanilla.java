package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import io.ebean.SqlUpdate;
import org.tests.model.basic.MRole;
import org.tests.model.basic.MUser;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestM2MVanilla extends BaseTestCase {

  @Test
  public void testVanilla() {

    SqlUpdate delInt = Ebean.createSqlUpdate("delete from mrole_muser");
    SqlUpdate delRoles = Ebean.createSqlUpdate("delete from mrole");
    SqlUpdate delUsers = Ebean.createSqlUpdate("delete from muser");

    Ebean.execute(delInt);
    Ebean.execute(delRoles);
    Ebean.execute(delUsers);

    MRole r1 = new MRole();
    r1.setRoleName("role1");
    Ebean.save(r1);

    MRole r2 = new MRole();
    r2.setRoleName("role2");
    Ebean.save(r2);

    MRole r3 = new MRole();
    r3.setRoleName("role3");
    Ebean.save(r3);

    MUser u0 = new MUser();
    u0.setUserName("something");

    Ebean.save(u0);

    MUser user = Ebean.find(MUser.class, u0.getUserid());

    List<MRole> roleList = new ArrayList<>();
    roleList.add(r1);
    roleList.add(r2);

    user.setRoles(roleList);

    Ebean.save(user);
    // Ebean.saveManyToManyAssociations(user, "roles");

    MUser checkUser = Ebean.find(MUser.class, u0.getUserid());
    List<MRole> checkRoles = checkUser.getRoles();
    Assert.assertNotNull(checkRoles);
    Assert.assertEquals(2, checkRoles.size());

    checkRoles.add(r3);

    Ebean.save(checkUser);
    // Ebean.saveManyToManyAssociations(checkUser, "roles");

    MUser checkUser2 = Ebean.find(MUser.class, u0.getUserid());
    List<MRole> checkRoles2 = checkUser2.getRoles();
    Assert.assertNotNull(checkRoles2);
    Assert.assertEquals("added a role", 3, checkRoles2.size());

    Query<MUser> rolesQuery0 = Ebean.find(MUser.class).where().eq("roles", r1).query();

    rolesQuery0.findList();

    Query<MUser> rolesQuery = Ebean.find(MUser.class).where().in("roles", roleList).query();

    List<MUser> userInRolesList = rolesQuery.findList();
    Assert.assertTrue(!userInRolesList.isEmpty());

    List<MUser> list = Ebean.find(MUser.class)
      .where().in("roles", roleList)
      .filterMany("roles").eq("roleName", "role1")
      .findList();

    MUser mUser = list.get(0);
    List<MRole> roles = mUser.getRoles();
    Assert.assertEquals(1, roles.size());

    checkRoles2.remove(0);
    checkRoles2.remove(0);
    Ebean.save(checkUser2);

    checkUser2 = Ebean.find(MUser.class, u0.getUserid());
    checkRoles2 = checkUser2.getRoles();
    Assert.assertNotNull(checkRoles2);
    Assert.assertEquals("added a role", 1, checkRoles2.size());

    Ebean.delete(checkUser2);

  }

}
