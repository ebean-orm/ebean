package org.tests.model.inheritexposedtype.test;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.inheritexposedtype.IXPhoto;

public class TestInheritExposeDtype extends BaseTestCase {

  @Test
  public void test() {

    IXPhoto p = new IXPhoto();
    p.setName("the name");
    DB.save(p);

    // update
    p.setName("new name");
    DB.save(p);

    // delete
    DB.delete(p);

  }
}
