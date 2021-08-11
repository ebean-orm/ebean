package org.tests.m2m;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.Test;
import org.tests.model.basic.MRole;
import org.tests.model.basic.MUser;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestM2MModifyTest extends BaseTestCase {

  @Test
  public void test() {

    // Create 2 roles r0 and r1
    MRole r0 = new MRole("r0");
    MRole r1 = new MRole("r1");

    // Save r1 and r2
    DB.save(r0);
    DB.save(r1);

    // Create a new user
    MUser u0 = new MUser("usr0");
    u0.addRole(r0);
    u0.addRole(r1);

    // Save the user
    DB.save(u0);

    List<MRole> roles = u0.getRoles();

    assertThat(roles).hasSize(2);

    u0 = DB.find(MUser.class, u0.getUserid());

    roles = u0.getRoles();
    assertThat(roles).hasSize(2);

    roles.clear();
    roles.add(r0);
    roles.add(r1);
    roles.remove(r1);

    DB.save(u0);

    u0 = DB.find(MUser.class, u0.getUserid());

    roles = u0.getRoles();
    assertThat(roles).hasSize(1);
  }
}
