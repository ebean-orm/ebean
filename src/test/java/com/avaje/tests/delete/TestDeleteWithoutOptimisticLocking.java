package com.avaje.tests.delete;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Contact;
import org.junit.Test;

public class TestDeleteWithoutOptimisticLocking extends BaseTestCase {

  @Test
  public void test() {

    // delete by by without version loaded ... should not throw OptimisticLockException
    Contact ref = Ebean.getReference(Contact.class, 999999);
    Ebean.delete(ref);

    Ebean.delete(Contact.class, 999999);
  }

}
