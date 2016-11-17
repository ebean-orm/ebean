package com.avaje.tests.query;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Customer;
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
