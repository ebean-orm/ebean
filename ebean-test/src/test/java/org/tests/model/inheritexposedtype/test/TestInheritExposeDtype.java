package org.tests.model.inheritexposedtype.test;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.tests.model.inheritexposedtype.IXPhoto;
import org.junit.jupiter.api.Test;

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
