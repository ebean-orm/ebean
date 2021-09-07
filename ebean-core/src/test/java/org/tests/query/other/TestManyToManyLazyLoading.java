package org.tests.query.other;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.tests.model.basic.MRole;
import org.tests.model.basic.MUser;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TestManyToManyLazyLoading extends BaseTestCase {

  @Test
  public void test() {

    createData();

    List<MUser> users = DB.find(MUser.class).findList();

    for (MUser user : users) {
      List<MRole> roles = user.getRoles();
      roles.size();
    }

  }

  private void createData() {
    MRole r0 = new MRole("r0");
    MRole r1 = new MRole("r1");

    DB.save(r0);
    DB.save(r1);

    MUser u0 = new MUser("usr0");
    u0.addRole(r0);
    u0.addRole(r1);

    DB.save(u0);
  }

}
