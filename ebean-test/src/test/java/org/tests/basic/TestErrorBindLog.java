package org.tests.basic;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;

import javax.persistence.PersistenceException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestErrorBindLog extends BaseTestCase {

  @Test
  public void test() {

    try {
      DB.find(Order.class).where().gt("id", "JUNK").findList();

    } catch (PersistenceException e) {
      String msg = e.getMessage();
      if (isHana()) {
        assertTrue(msg.contains("Error with property[1] dt[12]data[JUNK]"));
      }
      else {
        assertTrue(msg.contains("Bind values:"));
      }
    }
  }
}
