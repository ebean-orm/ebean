package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestInEmpty extends BaseTestCase {

  @Test
  public void test_in_empty() {

    Query<Order> query = DB.find(Order.class).where().in("id", new Object[0]).gt("id", 0)
      .query();

    List<Order> list = query.findList();
    assertSql(query).contains("1=0");
    assertEquals(0, list.size());
  }

  @Test
  public void test_isIn_empty() {

    Query<Order> query = DB.find(Order.class).where().isIn("id", new Object[0]).gt("id", 0)
      .query();

    List<Order> list = query.findList();
    assertSql(query).contains("1=0");
    assertEquals(0, list.size());
  }

  @Test
  public void test_notIn_empty() {

    Query<Order> query = DB.find(Order.class).where().notIn("id", new Object[0]).gt("id", 0)
      .query();

    query.findList();
    assertSql(query).contains("1=1");
  }

}
