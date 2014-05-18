package com.avaje.tests.query;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestRowCount extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Query<Order> query = Ebean.find(Order.class).fetch("details").where().gt("id", 1)
        .gt("details.id", 1).order("id desc");

    int rc = query.findRowCount();
    System.out.println("rc:" + rc);

    List<Object> ids = query.findIds();
    System.out.println("ids:" + ids);

    List<Order> list = query.findList();
    System.out.println(list);

    Assert.assertEquals("same rc to ids.size() ", rc, ids.size());
    Assert.assertEquals("same rc to list.size()", rc, list.size());
  }

}
