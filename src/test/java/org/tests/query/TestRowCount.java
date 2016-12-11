package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestRowCount extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Query<Order> query = Ebean.find(Order.class).fetch("details").where().gt("id", 1)
      .gt("details.id", 1).order("id desc");

    int rc = query.findCount();

    List<Object> ids = query.findIds();

    List<Order> list = query.findList();
    System.out.println(list);
    for (Order order : list) {
      order.getStatus();
    }

    Assert.assertEquals("same rc to ids.size() ", rc, ids.size());
    Assert.assertEquals("same rc to list.size()", rc, list.size());
  }

}
