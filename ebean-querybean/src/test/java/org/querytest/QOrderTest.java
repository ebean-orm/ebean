package org.querytest;

import io.ebean.DB;
import io.ebean.FetchGroup;
import io.ebean.test.LoggedSql;
import org.example.domain.Customer;
import org.example.domain.Order;
import org.example.domain.otherpackage.PhoneNumber;
import org.example.domain.query.QCustomer;
import org.example.domain.query.QOrder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class QOrderTest {

  private static final QCustomer cu = QCustomer.alias();

  private static final QOrder or = QOrder.alias();

  private static final FetchGroup<Customer> fgC = QCustomer.forFetchGroup()
    .select(cu.name, cu.phoneNumber)
    .buildFetchGroup();

  private static final FetchGroup<Order> fgNested1 = QOrder.forFetchGroup()
    .select(or.status, or.shipDate)
    .customer.fetch(fgC)
    .buildFetchGroup();

  private static final FetchGroup<Order> fgNested_fetchQuery = QOrder.forFetchGroup()
    .select(or.status)
    .customer.fetchQuery(fgC)
    .buildFetchGroup();

  private static final FetchGroup<Order> fgNested_fetchCache = QOrder.forFetchGroup()
    .select(or.status)
    .customer.fetchCache(fgC)
    .buildFetchGroup();


  private static final FetchGroup<Order> fg = QOrder.forFetchGroup()
    .select(or.status, or.shipDate)
    .customer.fetchCache(cu.name, cu.status, cu.registered, cu.comments)
    .buildFetchGroup();

  private static final FetchGroup<Order> fg2 = QOrder.forFetchGroup()
    .select(or.status)
    .customer.fetch(cu.name)
    .buildFetchGroup();

  private static Order order;
  private static Customer customer;

  @BeforeClass
  public static void before() {
    setupData();
  }

  @AfterClass
  public static void after() {
    DB.delete(order);
    DB.delete(customer);
  }

  @Test
  public void fetchCache() {

    new QOrder()
      .status.eq(Order.Status.NEW)
      .customer.fetchCache(cu.name, cu.registered)
      .findList();

    new QOrder()
      .status.eq(Order.Status.NEW)
      .customer.fetchCache()
      .findList();
  }

  @Test
  public void viaFetchGraph() {

    DB.getDefault();
    LoggedSql.start();

    new QOrder()
      .status.eq(Order.Status.NEW)
      .select(fg)
      .findList();


    final List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("select t0.id, t0.status, t0.ship_date, t0.customer_id from o_order t0 where");
  }

  @Test
  public void viaFetchGraph_withJoin() {

    DB.getDefault();
    LoggedSql.start();

    new QOrder()
      .status.eq(Order.Status.NEW)
      .select(fg2)
      .findList();


    final List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("select t0.id, t0.status, t1.id, t1.name from o_order t0 join be_customer t1 on t1.id = t0.customer_id where");
  }

  @Test
  public void viaFetchGraph_withNested() {

    DB.getDefault();
    LoggedSql.start();

    new QOrder()
      .status.eq(Order.Status.NEW)
      .select(fgNested1)
      .findList();

    final List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("select t0.id, t0.status, t0.ship_date, t1.id, t1.name, t1.phone_number from o_order t0 join be_customer t1 on t1.id = t0.customer_id where");
  }

  @Test
  public void viaFetchGraph_withNested_fetchQuery() {

    DB.getDefault();
    LoggedSql.start();

    final Order found = new QOrder()
      .id.eq(order.getId())
      .select(fgNested_fetchQuery)
      .findOne();

    final List<String> sql = LoggedSql.stop();

    // assert fetching customer via fetchQuery
    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("select t0.id, t0.status, t0.customer_id from o_order t0 where t0.id = ?");
    assertThat(sql.get(1)).contains("select t0.id, t0.name, t0.phone_number from be_customer t0 where t0.id = ?");

    assertThat(found.getCustomer().getPhoneNumber().getMsisdn()).isEqualTo("Ph1");
  }


  @Test
  public void viaFetchGraph_withNested_fetchCache() {

    DB.getDefault();

    // ensure the customer is loaded in the L2 cache
    new QCustomer().id.eq(customer.getId()).findOne();

    LoggedSql.start();

    final Order found = new QOrder()
      .id.eq(order.getId())
      .select(fgNested_fetchCache) // cache hit for customer
      .findOne();

    final String msisdn = found.getCustomer().getPhoneNumber().getMsisdn();
    assertThat(msisdn).isEqualTo("Ph1");

    final List<String> sql = LoggedSql.stop();

    // assert we only hit DB for order
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("select t0.id, t0.status, t0.customer_id from o_order t0 where t0.id = ?");
  }

  @Test
  public void select_partial() {

    DB.getDefault();
    LoggedSql.start();

    final QOrder o = QOrder.alias();

    new QOrder()
      .select(o.status, o.orderDate)
      .findList();

    final List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("select t0.id, t0.status, t0.order_date from o_order t0");
  }

  @Test
  public void fetch_partial() {

    DB.getDefault();
    LoggedSql.start();

    final QOrder o = QOrder.alias();
    final QCustomer c = QCustomer.alias();

    new QOrder()
      .select(o.status)
      .customer.fetch(c.email, c.name)
      .findList();

    final List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("select t0.id, t0.status, t1.id, t1.email, t1.name from o_order t0 join be_customer t1 on t1.id = t0.customer_id");

  }

  private static void setupData() {

    customer = new Customer();
    customer.setName("Fred");
    customer.setPhoneNumber(new PhoneNumber("Ph1"));
    customer.save();

    order = new Order();
    order.setCustomer(customer);
    order.save();
  }
}
