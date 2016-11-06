package com.avaje.tests.enhancement;

import com.avaje.ebean.BaseTestCase;
import com.avaje.tests.model.basic.MRole;
import com.avaje.tests.model.basic.MUser;
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
