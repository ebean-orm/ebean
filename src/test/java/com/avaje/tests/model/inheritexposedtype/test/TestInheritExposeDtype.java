package com.avaje.tests.model.inheritexposedtype.test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.inheritexposedtype.IXPhoto;
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
