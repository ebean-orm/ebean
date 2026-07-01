package org.tests.query;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;

import jakarta.persistence.PersistenceException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class TestInvalidFetchPath extends BaseTestCase {

  @Test
  void invalidFetchPathAndProperties_expect_error() {
    assertThrows(PersistenceException.class, () ->
      DB.find(Customer.class)
        .fetch("notValidPath", "notHaveProps")
        .findList());
  }

  @Test
  void invalidFetchPath_expect_error() {
    assertThrows(PersistenceException.class, () ->
      DB.find(Customer.class)
        .fetch("notValidPath")
        .findList());
  }

  @Test
  void fetchWithInvalidPropertyName_expect_error() {
    assertThrows(PersistenceException.class, () ->
      DB.find(Customer.class)
        .fetch("billingAddress", "invalidPropertyName")
        .findList());
  }
}
