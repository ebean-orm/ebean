package com.avaje.tests.basic;

import javax.persistence.PersistenceException;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.config.GlobalProperties;
import com.avaje.tests.model.basic.Order;

public class TestErrorBindLog extends BaseTestCase {

  @Test
  public void test() {

    GlobalProperties.put("somethingelse", "d:/junk2");
    try {
      Ebean.find(Order.class).where().gt("id", "JUNK").findList();

    } catch (PersistenceException e) {
      String msg = e.getMessage();
      e.printStackTrace();
      Assert.assertTrue(msg.contains("Bind values:"));
    }
  }
}
