package com.avaje.tests.query;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Customer;
import org.junit.Test;

public class TestInvalidFetchPath extends BaseTestCase {

  @Test
  public void testWithPathAndProperties() {

    Ebean.find(Customer.class)
            .fetch("notHaveProps", "notHaveProps")
            .findList();

  }

}
