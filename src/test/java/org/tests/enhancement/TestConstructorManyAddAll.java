package org.tests.enhancement;

import io.ebean.BaseTestCase;
import org.tests.model.basic.MRole;
import org.tests.model.basic.MUser;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestConstructorManyAddAll extends BaseTestCase {

  @Test
  public void test() {

    List<MRole> startRoles = new ArrayList<>();
    MUser mUser = new MUser(startRoles);
    mUser.getRoles();
  }
}
