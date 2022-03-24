package org.tests.enhancement;

import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.MRole;
import org.tests.model.basic.MUser;

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
