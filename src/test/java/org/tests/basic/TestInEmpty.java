package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.junit.Assert.assertEquals;

public class TestInEmpty extends BaseTestCase {

  @Test
  public void test_in_empty() {
    ResetBasicData.reset();

    Query<Order> query = Ebean.find(Order.class).where().in("id", new Object[0]).gt("id", 0)
      .query();

    List<Order> list = query.findList();
    assertThat(query.getGeneratedSql()).contains("1=0");
    assertEquals(0, list.size());
  }

  @Test
  public void test_isIn_empty() {
    ResetBasicData.reset();

    Query<Order> query = Ebean.find(Order.class).where().isIn("id", new Object[0]).gt("id", 0)
      .query();

    List<Order> list = query.findList();
    assertThat(query.getGeneratedSql()).contains("1=0");
    assertEquals(0, list.size());
  }

  @Test
  public void test_notIn_empty() {
    ResetBasicData.reset();

    Query<Order> query = Ebean.find(Order.class).where().notIn("id", new Object[0]).gt("id", 0)
      .query();

    query.findList();
    assertThat(query.getGeneratedSql()).contains("1=1");
  }

}
