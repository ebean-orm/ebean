package org.tests.m2m;

import io.ebean.DB;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.MRole;
import org.tests.model.basic.MUser;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestM2MSortTest extends BaseTestCase {

  @Test
  public void test() {
    // Create 2 roles r0 and r1
    MRole r0 = new MRole("r0");
    MRole r1 = new MRole("r1");

    // Save r1 and r2
    DB.save(r0);
    DB.save(r1);

    // Create new users
    MUser u0 = new MUser("usr0");
    u0.addRole(r0);
    u0.addRole(r1);
    MUser u1 = new MUser("usr1");
    u1.addRole(r0);

    // Save the users
    DB.save(u0);
    DB.save(u1);

    var users = DB.find(MUser.class).orderBy("roles").findList();
    assertThat(users).hasSize(2);
  }
}
