package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.junit.Assert;
import org.junit.Test;
import org.tests.model.basic.Order;

import javax.persistence.PersistenceException;

public class TestErrorBindLog extends BaseTestCase {

  @Test
  public void test() {

    try {
      Ebean.find(Order.class).where().gt("id", "JUNK").findList();

    } catch (PersistenceException e) {
      String msg = e.getMessage();
      if (isHana()) {
        Assert.assertTrue(msg.contains("Error with property[1] dt[12]data[JUNK]"));
      }
      else {
        Assert.assertTrue(msg.contains("Bind values:"));
      }
    }
  }
}
