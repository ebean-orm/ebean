package org.tests.basic;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;

import jakarta.persistence.PersistenceException;

import static org.assertj.core.api.Assertions.assertThat;

class TestErrorBindLog extends BaseTestCase {

  @Test
  void test() {
    try {
      DB.find(Order.class).where().gt("id", "JUNK").findList();
    } catch (PersistenceException e) {
      String msg = e.getMessage();
      if (isHana()) {
        assertThat(msg).contains("Error with property");
      } else {
        assertThat(msg).contains("Bind values:");
      }
    }
  }
}
