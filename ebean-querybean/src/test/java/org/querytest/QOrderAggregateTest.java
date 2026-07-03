package org.querytest;

import io.ebean.DB;
import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;
import io.ebean.test.LoggedSql;
import org.example.domain.Customer;
import org.example.domain.Order;
import org.example.domain.OrderAggregate;
import org.example.domain.OrderDetail;
import org.example.domain.Product;
import org.example.domain.query.QOrderAggregate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Demonstrates that RawSql (including RawSqlBuilder.withPlaceholders()) can be used
 * together with a generated, type-safe query bean (QOrderAggregate) - not just the
 * plain Query&lt;T&gt; API.
 */
class QOrderAggregateTest {

  /** CTE with a ${where} placeholder so it can't be parsed by RawSqlBuilder.parse(). */
  private static final String SQL =
    "with order_totals as (" +
    "  select o.id as order_id," +
    "         sum(d.order_qty * d.unit_price) as total_amount" +
    "  from o_order o" +
    "  join o_order_detail d on d.order_id = o.id" +
    "  group by o.id" +
    ")" +
    " select order_id, total_amount" +
    " from order_totals" +
    " ${where}" +
    " order by order_id";

  private static Order order1;
  private static Order order2;

  @BeforeAll
  static void setup() {
    Customer customer = new Customer();
    customer.setName("QOrderAggregateTest customer");
    customer.save();

    Product product = new Product();
    product.setName("prod1");
    product.setSku("qoat-1");
    product.save();

    order1 = new Order();
    order1.setCustomer(customer);
    order1.getDetails().add(new OrderDetail(product, 2, 20.0)); // total 40.00
    order1.save();

    order2 = new Order();
    order2.setCustomer(customer);
    order2.getDetails().add(new OrderDetail(product, 5, 20.0)); // total 100.00
    order2.save();
  }

  @AfterAll
  static void tearDown() {
    DB.delete(order1);
    DB.delete(order2);
  }

  @Test
  void queryBean_setRawSql_noFilter_returnsAllRows() {
    RawSql rawSql = RawSqlBuilder.withPlaceholders(SQL)
      .columnMapping("order_id", "order.id")
      .columnMapping("total_amount", "totalAmount")
      .create();

    List<OrderAggregate> list = new QOrderAggregate()
      .setRawSql(rawSql)
      .findList();

    assertThat(list).hasSizeGreaterThanOrEqualTo(2);
  }

  @Test
  void queryBean_setRawSql_withTypedWhereExpression_filtersRows() {
    RawSql rawSql = RawSqlBuilder.withPlaceholders(SQL)
      .columnMapping("order_id", "order.id")
      .columnMapping("total_amount", "totalAmount")
      .create();

    // typed property expression (totalAmount.gt(...)) rather than the string based where().gt(...)
    List<OrderAggregate> list = new QOrderAggregate()
      .setRawSql(rawSql)
      .totalAmount.gt(50.0)
      .findList();

    assertThat(list).extracting(OrderAggregate::getOrder)
      .extracting(Order::getId)
      .contains(order2.getId())
      .doesNotContain(order1.getId());
  }

  @Test
  void queryBean_setRawSql_verifySqlStructure() {
    RawSql rawSql = RawSqlBuilder.withPlaceholders(SQL)
      .columnMapping("order_id", "order.id")
      .columnMapping("total_amount", "totalAmount")
      .create();

    LoggedSql.start();
    new QOrderAggregate()
      .setRawSql(rawSql)
      .totalAmount.gt(50.0)
      .findList();
    List<String> sql = LoggedSql.stop();

    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).containsIgnoringCase("where total_amount > ?");
  }
}
