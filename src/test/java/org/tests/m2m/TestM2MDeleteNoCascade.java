package org.tests.m2m;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.MnocRole;
import org.tests.model.basic.MnocUser;
import org.junit.Assert;
import org.junit.Test;

import javax.persistence.PersistenceException;
import java.util.List;

public class TestM2MDeleteNoCascade extends BaseTestCase {

  @Test
  public void test() {

    MnocRole r0 = new MnocRole("r0");
    MnocRole r1 = new MnocRole("r0");

    Ebean.save(r0);
    Ebean.save(r1);

    MnocUser u0 = new MnocUser("usr0");
    u0.addValidRole(r0);
    u0.addValidRole(r1);

    Ebean.save(u0);

    MnocUser loadedUser = Ebean.find(MnocUser.class, u0.getUserId());
    List<MnocRole> validRoles = loadedUser.getValidRoles();

    Assert.assertEquals(2, validRoles.size());

    try {
      Ebean.delete(u0);
      Assert.assertTrue("expecting an exception", false);

    } catch (PersistenceException e) {
      // we expect this
      Assert.assertTrue(true);
    }

    u0.getValidRoles().clear();
    Ebean.save(u0);

    Ebean.delete(u0);
  }
}
