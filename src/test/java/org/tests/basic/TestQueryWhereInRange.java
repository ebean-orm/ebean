package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.junit.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.time.LocalDate;

public class TestQueryWhereInRange extends BaseTestCase {

  @Test
  public void inRange() {

    ResetBasicData.reset();

    LocalDate today = LocalDate.now();

    Query<Order> query= Ebean.find(Order.class)
      .where().inRange("orderDate", today.minusDays(7), today)
      .isNotNull("id")
      .query();

    query.findList();

    sqlOf(query).contains("(order_date >= ? and order_date < ?)");

  }
}
