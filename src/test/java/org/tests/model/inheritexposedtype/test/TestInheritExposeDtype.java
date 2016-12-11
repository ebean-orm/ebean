package org.tests.model.inheritexposedtype.test;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.inheritexposedtype.IXPhoto;
import org.junit.Test;

public class TestInheritExposeDtype extends BaseTestCase {

  @Test
  public void test() {

    IXPhoto p = new IXPhoto();
    p.setName("the name");
    Ebean.save(p);

    // update
    p.setName("new name");
    Ebean.save(p);

    // delete
    Ebean.delete(p);

  }
}
