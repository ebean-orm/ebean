package org.tests.query;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;

import javax.persistence.PersistenceException;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestInvalidFetchPath extends BaseTestCase {

  @Test
  public void invalidFetchPathAndProperties_expect_error() {
    assertThrows(PersistenceException.class, () ->
      DB.find(Customer.class)
        .fetch("notValidPath", "notHaveProps")
        .findList());
  }

  @Test
  public void invalidFetchPath_expect_error() {
    assertThrows(PersistenceException.class, () ->
      DB.find(Customer.class)
        .fetch("notValidPath")
        .findList());
  }

  @Test
  public void fetchWithInvalidPropertyName_expect_allowed() {
    DB.find(Customer.class)
      .fetch("billingAddress", "invalidPropertyName")
      .findList();
  }
}
