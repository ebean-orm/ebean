package org.tests.model.basic.test;

import io.ebean.DB;
import io.ebean.Query;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RawSqlOrmQueryTest extends BaseTestCase {

  //@IgnorePlatform(Platform.ORACLE)
  @Test
  public void testNamed() {

    Query<Order> query = DB.getDefault().createNamedQuery(Order.class, "myRawTest");
    query.setParameter("orderStatus", Order.Status.NEW);
    query.setMaxRows(10);
    List<Order> list = query.findList();
    for (Order order : list) {
      order.getWhenCreated();
    }

    String sql = query.getGeneratedSql();
    if (isSqlServer()) {
      assertThat(sql).contains("select top 10 o.id,");
    } else {
      assertThat(sql).contains("select o.id,");
      assertThat(sql).contains("limit 10");
    }
    assertThat(sql).contains("o.id, o.status, o.ship_date, c.id, c.name, a.id, a.line1, a.line2, a.city from o_order o");
    assertThat(sql).contains("join o_customer c on o.customer_id = c.id ");
    assertThat(sql).contains("where o.status = ?  order by c.name, c.id");
  }

  //@IgnorePlatform(Platform.ORACLE)
  @Test
  public void testNamed_fromCustomXmlLocations() {

    //ResetBasicData.reset();

    Query<Order> query = DB.getDefault().createNamedQuery(Order.class, "myRawTest2");
    query.setParameter("orderStatus", Order.Status.NEW);
    query.setMaxRows(10);
    List<Order> list = query.findList();
    for (Order order : list) {
      order.getWhenModified();
    }

    String sql = query.getGeneratedSql();
    if (isSqlServer()) {
      assertThat(sql).contains("select top 10 o.id,");
    } else {
      assertThat(sql).contains("select o.id,");
      assertThat(sql).contains("limit 10");
    }
    assertThat(sql).contains("o.id, o.status, o.ship_date, c.id, c.name, a.id, a.line1, a.line2, a.city from o_order o");
    assertThat(sql).contains("join o_customer c on o.customer_id = c.id ");
    assertThat(sql).contains("where o.status = ?  order by c.name, c.id");
  }

  //@IgnorePlatform(Platform.ORACLE)
  @Test
  public void testNamed_fromCustomXmlLocations_withComments() {

    //ResetBasicData.reset();

    Query<Order> query = DB.getDefault().createNamedQuery(Order.class, "myRawTest3");
    query.setMaxRows(10);

    query.findList();
    assertThat(query.getGeneratedSql()).contains("-- must be unparsed raw sql");
  }

}
