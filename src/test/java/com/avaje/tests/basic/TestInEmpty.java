package com.avaje.tests.basic;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Order;

public class TestInEmpty extends BaseTestCase {

  @Test
  public void test() {

    List<Order> list = Ebean.find(Order.class).where().gt("id", 0).in("id", new Object[0])
        .findList();

    Assert.assertEquals(0, list.size());

  }

}
