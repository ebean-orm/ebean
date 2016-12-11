package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.Customer;
import org.junit.Test;

import javax.persistence.PersistenceException;

public class TestInvalidFetchPath extends BaseTestCase {

  @Test(expected = PersistenceException.class)
  public void invalidFetchPathAndProperties_expect_error() {

    Ebean.find(Customer.class)
      .fetch("notValidPath", "notHaveProps")
      .findList();
  }

  @Test(expected = PersistenceException.class)
  public void invalidFetchPath_expect_error() {

    Ebean.find(Customer.class)
      .fetch("notValidPath")
      .findList();
  }

  @Test
  public void fetchWithInvalidPropertyName_expect_allowed() {

    Ebean.find(Customer.class)
      .fetch("billingAddress", "invalidPropertyName")
      .findList();
  }
}
