package org.tests.model.m2m;

import io.ebean.Ebean;
import org.junit.Test;

public class TestM2MDoubleInsert {

  @Test
  public void doubleInsert() {

    DRol role = new DRol("rol");
    role.save();

    DCredit credit = new DCredit("x1");
    credit.getDroles().add(role);
    role.getCredits().add(credit);
    credit.save();

    DRot rot = new DRot("rot");
    rot.getCroles().add(role);
    Ebean.save(rot);
  }
}
