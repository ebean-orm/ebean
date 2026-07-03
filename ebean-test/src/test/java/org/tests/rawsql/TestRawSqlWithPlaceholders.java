package org.tests.rawsql;

import io.ebean.DB;
import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.OrderAggregate;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for RawSqlBuilder.withPlaceholders() — complex SQL (CTEs,
 * window functions) with ${where} / ${andWhere} / ${having} / ${andHaving}
 * placeholders for dynamic WHERE and HAVING injection.
 */
class TestRawSqlWithPlaceholders extends BaseTestCase {

  /** CTE with ${where} in the outer SELECT — column names match the CTE output aliases. */
  private static final String CTE_SQL =
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

  /** Same CTE with ${andWhere} — a static WHERE clause is already present. */
  private static final String CTE_AND_WHERE_SQL =
    "with order_totals as (" +
    "  select o.id as order_id," +
    "         sum(d.order_qty * d.unit_price) as total_amount" +
    "  from o_order o" +
    "  join o_order_detail d on d.order_id = o.id" +
    "  group by o.id" +
    ")" +
    " select order_id, total_amount" +
    " from order_totals" +
    " where total_amount > 0 ${andWhere}" +
    " order by order_id";

  /** Direct aggregate (no CTE) with only a ${having} placeholder, and static ORDER BY after it. */
  private static final String HAVING_ONLY_SQL =
    "select o.id as order_id," +
    "       sum(d.order_qty * d.unit_price) as total_amount" +
    " from o_order o" +
    " join o_order_detail d on d.order_id = o.id" +
    " group by o.id" +
    " ${having}" +
    " order by order_id";

  /** Both ${where} and ${having} placeholders present, with static ORDER BY after the having. */
  private static final String WHERE_AND_HAVING_SQL =
    "select o.id as order_id," +
    "       sum(d.order_qty * d.unit_price) as total_amount" +
    " from o_order o" +
    " join o_order_detail d on d.order_id = o.id" +
    " ${where}" +
    " group by o.id" +
    " ${having}" +
    " order by order_id";

  private static RawSql cteSql;
  private static RawSql cteAndWhereSql;
  private static RawSql havingOnlySql;
  private static RawSql whereAndHavingSql;

  @BeforeAll
  static void setup() {
    ResetBasicData.reset();

    cteSql = RawSqlBuilder.withPlaceholders(CTE_SQL)
      .columnMapping("order_id", "order.id")
      .columnMapping("total_amount", "totalAmount")
      .create();

    cteAndWhereSql = RawSqlBuilder.withPlaceholders(CTE_AND_WHERE_SQL)
      .columnMapping("order_id", "order.id")
      .columnMapping("total_amount", "totalAmount")
      .create();

    havingOnlySql = RawSqlBuilder.withPlaceholders(HAVING_ONLY_SQL)
      .columnMapping("order_id", "order.id")
      .columnMapping("total_amount", "totalAmount")
      .create();

    whereAndHavingSql = RawSqlBuilder.withPlaceholders(WHERE_AND_HAVING_SQL)
      .columnMapping("order_id", "order.id")
      .columnMapping("total_amount", "totalAmount")
      .create();
  }

  @Test
  void withPlaceholders_noFilter_returnsAllRowsWithDetails() {
    List<OrderAggregate> list = DB.find(OrderAggregate.class)
      .setRawSql(cteSql)
      .findList();

    // orders 1, 2, 3 have details; orders 4 and 5 do not
    assertThat(list).hasSize(3);
    assertThat(list).extracting(OrderAggregate::getTotalAmount).doesNotContainNull();
  }

  @Test
  void withPlaceholders_withWhereFilter_returnsFilteredRows() {
    List<OrderAggregate> list = DB.find(OrderAggregate.class)
      .setRawSql(cteSql)
      .where().gt("totalAmount", 50)
      .findList();

    // order 1 ≈ 57.80, order 3 ≈ 165.50; order 2 = 42.00 is filtered out
    assertThat(list).hasSize(2);
    assertThat(list).extracting(OrderAggregate::getTotalAmount)
      .allMatch(amount -> amount > 50.0);
  }

  @Test
  void withPlaceholders_withStrongWhereFilter_returnsSingleRow() {
    List<OrderAggregate> list = DB.find(OrderAggregate.class)
      .setRawSql(cteSql)
      .where().gt("totalAmount", 100)
      .findList();

    // only order 3 has total > 100 (≈ 165.50)
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getTotalAmount()).isGreaterThan(100.0);
  }

  @Test
  void withPlaceholders_andWhere_appendsToExistingWhereClause() {
    List<OrderAggregate> list = DB.find(OrderAggregate.class)
      .setRawSql(cteAndWhereSql)
      .where().gt("totalAmount", 100)
      .findList();

    // ${andWhere} appends "and total_amount > 100" to the existing "where total_amount > 0"
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getTotalAmount()).isGreaterThan(100.0);
  }

  @Test
  void withPlaceholders_verifySqlStructure() {
    LoggedSql.start();
    DB.find(OrderAggregate.class)
      .setRawSql(cteSql)
      .where().gt("totalAmount", 50)
      .findList();
    List<String> sql = LoggedSql.stop();

    assertThat(sql).hasSize(1);
    String executed = sql.get(0);
    // SQL starts with the CTE — no spurious "select" prefix prepended
    assertThat(executed).containsIgnoringCase("with order_totals as");
    // WHERE is injected at the ${where} position (in the outer SELECT, before ORDER BY)
    assertThat(executed).containsIgnoringCase("where total_amount > ?");
    assertThat(executed).containsIgnoringCase("order by order_id");
    // WHERE appears after the CTE body
    int wherePos = executed.toLowerCase().lastIndexOf("where total_amount");
    int orderByPos = executed.toLowerCase().indexOf("order by order_id");
    assertThat(wherePos).isLessThan(orderByPos);
  }

  @Test
  void withPlaceholders_findCount() {
    int count = DB.find(OrderAggregate.class)
      .setRawSql(cteSql)
      .where().gt("totalAmount", 50)
      .findCount();

    assertThat(count).isEqualTo(2);
  }

  @Test
  void parse_failsOnCteSql() {
    // Demonstrates why withPlaceholders() is needed — parse() cannot handle CTEs
    assertThatThrownBy(() -> RawSqlBuilder.parse(CTE_SQL).create())
      .isInstanceOf(RuntimeException.class);
  }

  @Test
  void withPlaceholders_havingOnly_appliesBeforeStaticOrderBy() {
    LoggedSql.start();
    List<OrderAggregate> list = DB.find(OrderAggregate.class)
      .setRawSql(havingOnlySql)
      .having().gt("totalAmount", 100)
      .findList();
    List<String> sql = LoggedSql.stop();

    // only order 3 has aggregate total > 100 (≈ 165.50)
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getTotalAmount()).isGreaterThan(100.0);

    // the dynamically injected HAVING must appear before the static trailing ORDER BY,
    // otherwise the generated SQL would be invalid
    String executed = sql.get(0).toLowerCase();
    int havingPos = executed.indexOf("having");
    int orderByPos = executed.indexOf("order by");
    assertThat(havingPos).isGreaterThan(-1);
    assertThat(orderByPos).isGreaterThan(havingPos);
  }

  @Test
  void withPlaceholders_whereAndHaving_orderByTailNotLost() {
    LoggedSql.start();
    List<OrderAggregate> list = DB.find(OrderAggregate.class)
      .setRawSql(whereAndHavingSql)
      .where().gt("order.id", 0)
      .having().gt("totalAmount", 50)
      .findList();
    List<String> sql = LoggedSql.stop();

    // orders 1 (≈57.80) and 3 (≈165.50) pass the having filter; order 2 (=42.00) does not
    assertThat(list).hasSize(2);

    // both dynamic where and having are injected, and the static "order by order_id" tail
    // (positioned after ${having} in the template) is preserved rather than dropped
    String executed = sql.get(0).toLowerCase();
    assertThat(executed).contains("where");
    assertThat(executed).contains("having");
    assertThat(executed).contains("order by order_id");
    int havingPos = executed.indexOf("having");
    int orderByPos = executed.indexOf("order by");
    assertThat(orderByPos).isGreaterThan(havingPos);
  }
}
