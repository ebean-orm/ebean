package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.Order;
import org.junit.Assert;
import org.junit.Test;

import javax.persistence.PersistenceException;

public class TestErrorBindLog extends BaseTestCase {

  @Test
  public void test() {

    try {
      Ebean.find(Order.class).where().gt("id", "JUNK").findList();

    } catch (PersistenceException e) {
      String msg = e.getMessage();
      Assert.assertTrue(msg.contains("Bind values:"));
    }
  }
}
