package org.querytest;

import io.ebean.DB;
import io.ebean.FetchConfig;
import io.ebean.FetchGroup;
import io.ebean.Query;
import io.ebean.test.LoggedSql;
import org.example.domain.Customer;
import org.example.domain.Order;
import org.example.domain.OrderDetail;
import org.example.domain.otherpackage.PhoneNumber;
import org.example.domain.query.QContact;
import org.example.domain.query.QCustomer;
import org.example.domain.query.QOrder;
import org.example.domain.query.QOrderDetail;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.ebean.StdOperators.*;
import static org.assertj.core.api.Assertions.assertThat;

class QOrderTest {

  private static final QCustomer cu = QCustomer.alias();

  private static final QOrder or = QOrder.alias();

  private static final QContact co = QContact.alias();

  private static final FetchGroup<Customer> fgC = QCustomer.forFetchGroup()
    .select(cu.name, cu.phoneNumber)
    .buildFetchGroup();

  private static final FetchGroup<Customer> fgCustomerWithContacts = QCustomer.forFetchGroup()
    .select(cu.name, cu.phoneNumber)
    .contacts.fetch(FetchConfig.ofQuery(1000), co.firstName, co.lastName, co.email)
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

  @BeforeAll
  public static void before() {
    setupData();
  }

  @AfterAll
  public static void after() {
    DB.delete(order);
    DB.delete(customer);
  }

  @Test
  void orderById() {
    Query<Order> query = new QOrder()
      .select(QOrder.Alias.status)
      .orderById(true).query();

    query.findList();

    String sql = query.getGeneratedSql();
    assertThat(sql).contains("select /* QOrderTest.orderById:88 */ t0.id, t0.status from o_order t0 order by t0.id");

    Query<Order> query2 = new QOrder()
      .select(QOrder.Alias.status)
      .orderById(true).orderBy().status.asc()
      .query();

    query2.findList();

    String sql2 = query2.getGeneratedSql();
    assertThat(sql2).contains("select /* QOrderTest.orderById:98 */ t0.id, t0.status from o_order t0 order by t0.status, t0.id");
  }

  @Test
  void hint() {
    LoggedSql.start();
    new QCustomer()
      .setHint("FirstRows")
      .select(QCustomer.Alias.id, QCustomer.Alias.name)
      .findList();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("select /*+ FirstRows */ /* QOrderTest.hint */ t0.id, t0.name from be_customer t0");
  }

  @Test
  void fetchQueryWithBatch() {
    LoggedSql.start();

    new QCustomer()
      .select(fgCustomerWithContacts)
      .findList();

    final List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("select /* QOrderTest.fetchQueryWithBatch */ t0.id, t0.name, t0.phone_number from be_customer t0");
    assertThat(sql.get(1)).contains("select /* QOrderTest.fetchQueryWithBatch_contacts__query */ t0.customer_id, t0.id, t0.first_name, t0.last_name, t0.email from be_contact t0 where");
  }

  @Test
  void fetchCache() {

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
  void viaFetchGraph() {

    DB.getDefault();
    LoggedSql.start();

    new QOrder()
      .status.eq(Order.Status.NEW)
      .select(fg)
      .findList();


    final List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("select /* QOrderTest.viaFetchGraph */ t0.id, t0.status, t0.ship_date, t0.customer_id from o_order t0 where");
  }

  @Test
  void viaFetchGraph_withJoin() {

    DB.getDefault();
    LoggedSql.start();

    new QOrder()
      .status.eq(Order.Status.NEW)
      .select(fg2)
      .findList();


    final List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("select /* QOrderTest.viaFetchGraph_withJoin */ t0.id, t0.status, t1.id, t1.name from o_order t0 join be_customer t1 on t1.id = t0.customer_id where");
  }

  @Test
  void viaFetchGraph_withNested() {

    DB.getDefault();
    LoggedSql.start();

    new QOrder()
      .status.eq(Order.Status.NEW)
      .select(fgNested1)
      .findList();

    final List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("select /* QOrderTest.viaFetchGraph_withNested */ t0.id, t0.status, t0.ship_date, t1.id, t1.name, t1.phone_number from o_order t0 join be_customer t1 on t1.id = t0.customer_id where");
  }

  @Test
  void viaFetchGraph_withNested_fetchQuery() {
    DB.cacheManager().clearAll();
    LoggedSql.start();

    final Order found = new QOrder()
      .id.eq(order.getId())
      .select(fgNested_fetchQuery)
      .findOne();

    final List<String> sql = LoggedSql.stop();

    // assert fetching customer via fetchQuery
    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("select /* QOrderTest.viaFetchGraph_withNested_fetchQuery */ t0.id, t0.status, t0.customer_id from o_order t0 where t0.id = ?");
    assertThat(sql.get(1)).contains("select /* QOrderTest.viaFetchGraph_withNested_fetchQuery_customer__query */ t0.id, t0.name, t0.phone_number from be_customer t0 where t0.id = ?");

    assertThat(found.getCustomer().getPhoneNumber().getMsisdn()).isEqualTo("Ph1");
  }


  @Test
  void viaFetchGraph_withNested_fetchCache() {
    DB.cacheManager().clearAll();

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
    assertThat(sql.get(0)).contains("select /* QOrderTest.viaFetchGraph_withNested_fetchQuery */ t0.id, t0.status, t0.customer_id from o_order t0 where t0.id = ?");
  }

  @Test
  void select_partial() {

    DB.getDefault();
    LoggedSql.start();

    final QOrder o = QOrder.alias();

    new QOrder()
      .select(o.status, o.orderDate)
      .findList();

    final List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("select /* QOrderTest.select_partial */ t0.id, t0.status, t0.order_date from o_order t0");
  }

  @Test
  void fetch_partial() {

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
    assertThat(sql.get(0)).contains("select /* QOrderTest.fetch_partial */ t0.id, t0.status, t1.id, t1.email, t1.name from o_order t0 join be_customer t1 on t1.id = t0.customer_id");

  }

  @Test
  void updateQuery() {
    LoggedSql.start();
    new QOrder()
      .status.eq(Order.Status.COMPLETE)
      .orderDate.gt(new java.sql.Date(System.currentTimeMillis()))
      .asUpdate()
      .set(QOrder.Alias.version, 56L)
      .setNull(QOrder.Alias.orderDate)
      .update();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("update o_order set version=?, order_date=null where status = ? and order_date > ?");
  }

  @Test
  void stdExpression_iLikeConcatCoalesce() {
    QOrder o = QOrder.alias();

    // LOWER(CONCAT(COALESCE(a.name, ""), ":", a.description)) LIKE LOWER(:param)
    Query<Order> query = new QOrder()
      .select(o.id, o.status)
      .add(ilike(concat(coalesce(o.customer.name, "Not-Provided"), ":", o.status), "rob"))
      .query();

    query.findList();
    String generatedSql = query.getGeneratedSql();
    assertThat(generatedSql).contains("where lower(concat(coalesce(t1.name,'Not-Provided'),':',t0.status)) like ? escape''");
  }

  @Test
  void stdExpression_gtCoalesce() {
    QOrder o = QOrder.alias();

    Query<Order> query = new QOrder()
      .select(o.id, o.status)
      .or()
        .add(gt(coalesce(o.customer.version, 0), 42L))
        .id.lt(12)
      .endOr()
     .query();

    query.findList();

    String sql = query.getGeneratedSql();
    assertThat(sql).contains(" where (coalesce(t1.version,0) > ? or t0.id < ?)");
    assertThat(sql).isEqualTo("select /* QOrderTest.stdExpression_gtCoalesce */ t0.id, t0.status from o_order t0 join be_customer t1 on t1.id = t0.customer_id where (coalesce(t1.version,0) > ? or t0.id < ?)");
  }

  @Test
  void geSqlSubQuery() {
    var c = QCustomer.alias();

    var query = new QCustomer()
      .select(c.id)
      .registered.geSubQuery("select max(o.order_date) as foo from o_order o")
      .query();
    query.findList();

    assertThat(query.getGeneratedSql()).contains("select /* QOrderTest.geSqlSubQuery */ t0.id from be_customer t0 where t0.registered >= (select max(o.order_date) as foo from o_order o)");
  }

  @Test
  void gtSqlSubQuery() {
    var c = QCustomer.alias();

    var query = new QCustomer()
      .select(c.id)
      .registered.gtSubQuery("select max(o.order_date) as foo from o_order o")
      .query();
    query.findList();

    assertThat(query.getGeneratedSql()).contains("select /* QOrderTest.gtSqlSubQuery */ t0.id from be_customer t0 where t0.registered > (select max(o.order_date) as foo from o_order o)");
  }

  @Test
  void leSqlSubQuery() {
    var c = QCustomer.alias();

    var query = new QCustomer()
      .select(c.id)
      .registered.leSubQuery("select max(o.order_date) as foo from o_order o")
      .query();
    query.findList();

    assertThat(query.getGeneratedSql()).contains("select /* QOrderTest.leSqlSubQuery */ t0.id from be_customer t0 where t0.registered <= (select max(o.order_date) as foo from o_order o)");
  }

  @Test
  void ltSqlSubQuery() {
    var c = QCustomer.alias();

    var query = new QCustomer()
      .select(c.id)
      .registered.ltSubQuery("select max(o.order_date) as foo from o_order o")
      .query();
    query.findList();

    assertThat(query.getGeneratedSql()).contains("select /* QOrderTest.ltSqlSubQuery */ t0.id from be_customer t0 where t0.registered < (select max(o.order_date) as foo from o_order o)");
  }

  @Test
  void eqSqlSubQuery() {
    var c = QCustomer.alias();

    var query = new QCustomer()
      .select(c.id)
      .registered.eqSubQuery("select max(o.order_date) as foo from o_order o")
      .query();
    query.findList();

    assertThat(query.getGeneratedSql()).contains("select /* QOrderTest.eqSqlSubQuery */ t0.id from be_customer t0 where t0.registered = (select max(o.order_date) as foo from o_order o)");
  }

  @Test
  void neSqlSubQuery() {
    var c = QCustomer.alias();

    var query = new QCustomer()
      .select(c.id)
      .registered.neSubQuery("select max(o.order_date) as foo from o_order o")
      .query();
    query.findList();

    assertThat(query.getGeneratedSql()).contains("select /* QOrderTest.neSqlSubQuery */ t0.id from be_customer t0 where t0.registered <> (select max(o.order_date) as foo from o_order o)");
  }

  @Test
  void geSubQuery() {
    var c = QCustomer.alias();
    var o = QOrder.alias();

    var subQuery = new QOrder()
      .select(max(o.orderDate))
      .query();

    var query = new QCustomer()
      .select(c.id)
      .registered.ge(subQuery)
      .query();
    query.findList();

    assertThat(query.getGeneratedSql()).contains("select /* QOrderTest.geSubQuery */ t0.id from be_customer t0 where t0.registered >= (select max(t0.order_date) from o_order t0)");
  }

  @Test
  void gtSubQuery() {
    var subQuery = new QOrder()
      .select(sum(QOrder.Alias.version))
      .query();

    var query = new QCustomer()
      .select(QCustomer.Alias.id)
      .version.gt(subQuery)
      .query();
    query.findList();

    assertThat(query.getGeneratedSql()).contains("select /* QOrderTest.gtSubQuery */ t0.id from be_customer t0 where t0.version > (select sum(t0.version) from o_order t0)");
  }

  @Test
  void leSubQuery() {
    var subQuery = new QOrder()
      .select(max(QOrder.Alias.orderDate))
      .query();

    var query = new QCustomer()
      .select(QCustomer.Alias.id)
      .registered.le(subQuery)
      .query();
    query.findList();

    assertThat(query.getGeneratedSql()).contains("select /* QOrderTest.leSubQuery */ t0.id from be_customer t0 where t0.registered <= (select max(t0.order_date) from o_order t0)");
  }

  @Test
  void ltSubQuery() {
    var subQuery = new QOrder()
      .select(max(QOrder.Alias.orderDate))
      .query();

    var query = new QCustomer()
      .select(QCustomer.Alias.id)
      .registered.lt(subQuery)
      .query();
    query.findList();

    assertThat(query.getGeneratedSql()).contains("select /* QOrderTest.ltSubQuery */ t0.id from be_customer t0 where t0.registered < (select max(t0.order_date) from o_order t0)");
  }

  @Test
  void eqSubQuery() {
    var subQuery = new QOrder()
      .select("max(orderDate)")
      .query();

    var query = new QCustomer()
      .select(QCustomer.Alias.id)
      .registered.eq(subQuery)
      .query();
    query.findList();

    assertThat(query.getGeneratedSql()).contains("select /* QOrderTest.eqSubQuery */ t0.id from be_customer t0 where t0.registered = (select max(t0.order_date) from o_order t0)");
  }

  @Test
  void neSubQuery() {
    var subQuery = new QOrder()
      .select(max(QOrder.Alias.orderDate))
      .query();

    var query = new QCustomer()
      .select(QCustomer.Alias.id)
      .registered.ne(subQuery)
      .query();
    query.findList();

    assertThat(query.getGeneratedSql()).contains("select /* QOrderTest.neSubQuery */ t0.id from be_customer t0 where t0.registered <> (select max(t0.order_date) from o_order t0)");
  }

  @Test
  void propertyCompare() {
    Query<Order> query = new QOrder()
      .orderDate.lt(QOrder.Alias.shipDate) // have to be the exact same type, in this case java.sql.Date
      .query();

    query.findList();
    assertThat(query.getGeneratedSql()).contains(" from o_order t0 where t0.order_date < t0.ship_date");
  }

  @Test
  void subQueryExists() {
    Query<OrderDetail> subQuery = new QOrderDetail()
      .alias("od")
      .orderQty.gt(10)
      .id.eq(QOrder.Alias.details.id)
      //.where().raw("details.id = od.id")
      .query();

    Query<Order> query = new QOrder()
      .exists(subQuery)
      .orderBy("orderDate").query();

    query.findList();

    // select distinct t0.id, t0.status, t0.order_date, t0.ship_date, t0.version, t0.when_created, t0.when_updated, t0.customer_id, t0.shipping_address_id, t0.order_date
    // from o_order t0
    // left join o_order_detail t1 on t1.order_id = t0.id
    // where  exists (select 1 from o_order_detail od where od.order_qty > ? and od.id = t1.id) order by t0.order_date
    String sql = query.getGeneratedSql();
    assertThat(sql).contains(" exists (select 1 from o_order_detail od where od.order_qty > ? and od.id = t1.id)");
  }

  @Test
  void sqlSubQueryExists() {
    Query<Order> query = new QOrder()
      .exists("select 1 from o_order_detail od where od.order_qty > ? and od.order_id = t0.id", 10)
      .orderBy("orderDate").query();

    query.findList();

    String sql = query.getGeneratedSql();
    assertThat(sql).contains(" exists (select 1 from o_order_detail od where od.order_qty > ? and od.order_id = t0.id) order by t0.order_date");
  }

  @Test
  void sqlSubQueryNotExists() {
    Query<Order> query = new QOrder()
      .notExists("select 1 from o_order_detail od where od.order_qty > ? and od.order_id = t0.id", 10)
      .orderBy("orderDate").query();

    query.findList();

    String sql = query.getGeneratedSql();
    assertThat(sql).contains(" not exists (select 1 from o_order_detail od where od.order_qty > ? and od.order_id = t0.id) order by t0.order_date");
  }

  @Test
  void subQueryNotExists() {
    Query<OrderDetail> subQuery = new QOrderDetail()
      .alias("od")
      .orderQty.gt(10)
      .id.eq(QOrder.Alias.details.id)
      //.where().raw("details.id = od.id")
      .query();

    Query<Order> query = new QOrder()
      .notExists(subQuery)
      .orderBy("orderDate")
      .query();
    query.findList();

    // select distinct t0.id, t0.status, t0.order_date, t0.ship_date, t0.version, t0.when_created, t0.when_updated, t0.customer_id, t0.shipping_address_id, t0.order_date
    // from o_order t0
    // left join o_order_detail t1 on t1.order_id = t0.id
    // where not exists (select 1 from o_order_detail od where od.order_qty > ? and od.id = t1.id) order by t0.order_date
    String sql = query.getGeneratedSql();
    assertThat(sql).contains(" not exists (select 1 from o_order_detail od where od.order_qty > ? and od.id = t1.id)");
  }

  private static void setupData() {
    customer = new Customer();
    customer.name = "Fred";
    customer.phoneNumber = new PhoneNumber("Ph1");
    customer.save();

    order = new Order();
    order.setCustomer(customer);
    order.save();
  }

}
