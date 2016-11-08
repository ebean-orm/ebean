package com.avaje.tests.query.other;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.MRole;
import com.avaje.tests.model.basic.MUser;
import org.junit.Test;

import java.util.List;

public class TestManyToManyLazyLoading extends BaseTestCase {

  @Test
  public void test() {

    createData();

    List<MUser> users = Ebean.find(MUser.class).findList();

    for (MUser user : users) {
      List<MRole> roles = user.getRoles();
      roles.size();
    }

  }

  private void createData() {
    MRole r0 = new MRole("r0");
    MRole r1 = new MRole("r1");

    Ebean.save(r0);
    Ebean.save(r1);

    MUser u0 = new MUser("usr0");
    u0.addRole(r0);
    u0.addRole(r1);

    Ebean.save(u0);
  }

}
