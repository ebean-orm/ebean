package org.tests.rawsql;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;
import org.tests.model.basic.Customer;
import org.tests.model.basic.EBasic;
import org.tests.model.basic.Order;
import org.tests.model.basic.OrderDetail;
import org.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class TestRawSqlMasterDetail extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    String rs = "select t0.id, t0.status, t1.id, t1.name, " +
      " t2.id, t2.order_qty, t3.id, t3.name " +
      "from o_order t0 join o_customer t1 on t1.id = t0.kcustomer_id " +
      "join o_order_detail t2 on t2.order_id = t0.id  " +
      "join o_product t3 on t3.id = t2.product_id  " +
      "where t0.id <= :maxOrderId  and t3.id = :productId " +
      "order by t0.id, t2.id asc";

    RawSql rawSql = RawSqlBuilder.parse(rs)
      .columnMapping("t0.id", "id")
      .columnMapping("t0.status", "status")
      .columnMapping("t1.id", "customer.id")
      .columnMapping("t1.name", "customer.name")
      .columnMapping("t2.id", "details.id")
      .columnMapping("t2.order_qty", "details.orderQty")
      .columnMapping("t3.id", "details.product.id")
      .columnMapping("t3.name", "details.product.name")
      .create();

    List<Order> ordersFromRaw = Ebean.find(Order.class)
      .setRawSql(rawSql)
      .setParameter("maxOrderId", 2)
      .setParameter("productId", 1)
      .findList();

    printOrders(ordersFromRaw, "using RawSql");

  }

  @Test
  public void testForeignKeyColumn() {

    ResetBasicData.reset();

    // billing_address_id fk column is automatically
    // mapped to the logical path: billingAddress.id

    String rs = "select c.id, c.name, c.billing_address_id, c.updtime " +
      "from o_customer c " +
      "order by c.id";

    RawSql rawSql = RawSqlBuilder.parse(rs).create();

    List<Customer> customers = Ebean.find(Customer.class)
      .setRawSql(rawSql)
      .findList();

    assertThat(customers).isNotEmpty();
  }

  @Test
  public void testSimpleNestedColumn() {

    ResetBasicData.reset();


    String rs = "select c.id, c.name, c.billing_address_id, ba.line_1, ba.city, c.updtime " +
      "from o_customer c " +
      " left join o_address ba on ba.id = c.billing_address_id " +
      "order by c.id";

    RawSql rawSql = RawSqlBuilder.parse(rs)
      .tableAliasMapping("ba", "billingAddress")
      .create();

    List<Customer> customers = Ebean.find(Customer.class)
      .setRawSql(rawSql)
      .findList();

    assertThat(customers).isNotEmpty();
  }

  @Test
  public void testDoubleJoinColumn() {

    ResetBasicData.reset();

    String rs = "select c.id, c.name, c.billing_address_id, ba.line_1, ba.city, c.updtime, sa.id, sa.line_1, sa.city " +
      "from o_customer c " +
      " left join o_address ba on ba.id = c.billing_address_id " +
      " left join o_address sa on sa.id = c.shipping_address_id " +
      "order by c.id";

    RawSql rawSql = RawSqlBuilder.parse(rs)
      .tableAliasMapping("ba", "billingAddress")
      .tableAliasMapping("sa", "shippingAddress")
      .create();

    List<Customer> customers = Ebean.find(Customer.class)
      .setRawSql(rawSql)
      .findList();

    assertThat(customers).isNotEmpty();
  }

  @Test
  public void testWithTableAliasMapping() {

    ResetBasicData.reset();

    String rs = "select o.id, o.status, c.id, c.name, " +
      " d.id, d.order_qty, p.id, p.name " +
      "from o_order o join o_customer c on c.id = o.kcustomer_id " +
      "join o_order_detail d on d.order_id = o.id  " +
      "join o_product p on p.id = d.product_id  " +
      "where o.id <= :maxOrderId  and p.id = :productId " +
      "order by o.id, d.id asc";


    RawSql rawSql = RawSqlBuilder.parse(rs)
      .tableAliasMapping("c", "customer")
      .tableAliasMapping("d", "details")
      .tableAliasMapping("p", "details.product")
      .create();

    List<Order> ordersFromRaw = Ebean.find(Order.class)
      .setRawSql(rawSql)
      .setParameter("maxOrderId", 2)
      .setParameter("productId", 1)
      .findList();

    printOrders(ordersFromRaw, "using RawSql with tableAlias mapping");

  }

  @Test
  public void testWithNoIdPropertyWithInsert() {

    String name = "RawSql-NoIdTest" + new Random().nextInt();
    EBasic basic = new EBasic();
    basic.setName(name);
    basic.setStatus(EBasic.Status.ACTIVE);

    Ebean.save(basic);

    String rs = "select b.status, b.name from e_basic b ";

    RawSql rawSql = RawSqlBuilder.parse(rs).create();

    List<EBasic> list = Ebean.find(EBasic.class)
      .setRawSql(rawSql)
      .where().eq("name", name)
      .findList();

    assertEquals(1, list.size());
    EBasic basic1 = list.get(0);
    basic1.setDescription("insertAfterRawFetch");

    Ebean.save(basic1);
  }

  @Test
  public void testWithNoIdProperty() {

    ResetBasicData.reset();

    String rs = "select o.status, o.order_date from o_order o ";

    RawSql rawSql = RawSqlBuilder.parse(rs)
      .create();

    List<Order> ordersFromRaw = Ebean.find(Order.class)
      .setRawSql(rawSql)
      .findList();

    assertNotNull(ordersFromRaw);
    assertFalse(ordersFromRaw.isEmpty());
  }

  @Test
  public void testTableAlias_with_rootTypeMappedNotMapped() {

    ResetBasicData.reset();

    String rs = "select o.id, o.status " +
      "from o_order o order by o.id asc";

    RawSql rawSql = RawSqlBuilder.parse(rs)
      .create();

    List<Order> ordersFromRaw = Ebean.find(Order.class)
      .setRawSql(rawSql)
      .findList();

    for (Order order : ordersFromRaw) {
      assertThat(order.getId()).isNotNull();
      assertThat(order.getStatus()).isNotNull();
    }

  }

  @Test
  public void testTableAlias_with_rootTypeMappedToNullPath() {

    ResetBasicData.reset();

    String rs = "select o.id, o.status " +
      "from o_order o order by o.id asc";

    RawSql rawSql = RawSqlBuilder.parse(rs)
      .tableAliasMapping("o", null)
      .create();

    List<Order> ordersFromRaw = Ebean.find(Order.class)
      .setRawSql(rawSql)
      .findList();

    for (Order order : ordersFromRaw) {
      assertThat(order.getId()).isNotNull();
      assertThat(order.getStatus()).isNotNull();
    }
  }

  @Test
  public void testWithMultipleManys() {

    ResetBasicData.reset();

    String rs = "select o.id, o.status, c.id, c.name, " +
      " d.id, d.order_qty, p.id, p.name " +
      "from o_order o join o_customer c on c.id = o.kcustomer_id " +
      "join o_order_detail d on d.order_id = o.id  " +
      "join o_product p on p.id = d.product_id  " +
      "order by o.id, d.id asc";


    RawSql rawSql = RawSqlBuilder.parse(rs)
      .tableAliasMapping("c", "customer")
      .tableAliasMapping("d", "details")
      .tableAliasMapping("p", "details.product")
      .create();

    List<Order> ordersFromRaw = Ebean.find(Order.class)
      .setRawSql(rawSql)
      .findList();

    printOrders(ordersFromRaw, "using RawSql with tableAlias mapping");

  }

  private void printOrders(List<Order> orders, String heading) {

    for (Order order : orders) {
      List<OrderDetail> details = order.getDetails();
      order.getCustomer().getName();
      for (OrderDetail detail : details) {
        detail.getProduct().getId();
        detail.getOrderQty();
      }
    }
  }
}

