package org.tests.basic.delete;

import io.ebean.DB;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import io.ebeaninternal.api.SpiEbeanServer;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.OrderDetail;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TestDeleteCascadeById extends BaseTestCase {

  @Test
  void test() {

    ResetBasicData.reset();

    OrderDetail dummy = DB.reference(OrderDetail.class, 1);
    SpiEbeanServer server = (SpiEbeanServer) DB.getDefault();
    server.descriptor(OrderDetail.class).cacheBeanPut(dummy);

    Customer cust = ResetBasicData.createCustAndOrder("DelCas");
    assertNotNull(cust);

    List<Order> orders = DB.find(Order.class).where().eq("customer", cust).findList();

    assertEquals(1, orders.size());
    Order o = orders.get(0);
    assertNotNull(o);

    LoggedSql.start();

    DB.delete(o);
    List<String> sql = LoggedSql.collect();
    assertThat(sql).hasSize(6);
    assertThat(sql.get(0)).contains("select t0.id from o_order_detail t0 where order_id=?");
    assertThat(sql.get(1)).contains("delete from o_order_detail where id");
    assertThat(sql.get(2)).contains("-- bind(Array[3]");
    assertThat(sql.get(3)).contains("delete from or_order_ship where order_id = ?");
    assertThat(sql.get(4)).contains("-- bind(");
    assertThat(sql.get(5)).contains("delete from o_order where id=? and updtime=?");

    DB.delete(cust);
    sql = LoggedSql.stop();
    assertThat(sql).hasSize(9);
    assertThat(sql.get(0)).contains("select t0.id from contact t0 where customer_id=");
    assertThat(sql.get(1)).contains("delete from contact_note where (contact_id)");
    assertThat(sql.get(2)).contains(" -- bind(Array[3]=");
    assertThat(sql.get(3)).contains("delete from contact where id");
    assertThat(sql.get(4)).contains(" -- bind(Array[3]");
    assertThat(sql.get(5)).contains("delete from o_customer where id=? and version=?");
    assertThat(sql.get(6)).contains("delete from o_address where id=? and updtime=?");
    assertThat(sql.get(7)).contains(" -- bind(");
    assertThat(sql.get(8)).contains(" -- bind(");
  }
}
