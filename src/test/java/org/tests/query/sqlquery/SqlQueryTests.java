package org.tests.query.sqlquery;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Ebean;
import io.ebean.RowMapper;
import io.ebean.SqlQuery;
import io.ebean.SqlRow;
import io.ebean.meta.MetaTimedMetric;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class SqlQueryTests extends BaseTestCase {

  @Test
  public void findSingleAttributeList_decimal() {

    ResetBasicData.reset();

    String sql = "select (unit_price * order_qty) from o_order_detail where unit_price > ? order by (unit_price * order_qty) desc";

    List<BigDecimal> lineAmounts = DB.sqlQuery(sql)
      .setParameter(3)
      .mapToScalar(BigDecimal.class)
      .findList();

    assertThat(lineAmounts).isNotEmpty();
  }

  @Test
  public void findSingleDecimal() {

    ResetBasicData.reset();

    String sql = "select max(unit_price) from o_order_detail where order_qty > ?";

    BigDecimal maxPrice = DB.sqlQuery(sql)
      .setParameter(1, 2)
      .mapToScalar(BigDecimal.class)
      .findOne();

    assertThat(maxPrice).isNotNull();
  }

  @Test
  public void findSingleAttribute_BigDecimal() {

    ResetBasicData.reset();

    String sql = "select max(unit_price) from o_order_detail where order_qty > ?";

    BigDecimal maxPrice = DB.sqlQuery(sql)
      .setParameter(2)
      .mapToScalar(BigDecimal.class)
      .findOne();

    assertThat(maxPrice).isNotNull();
  }

  @Test
  public void findSingleLong() {

    ResetBasicData.reset();

    String sql = "select count(order_qty) from o_order_detail where unit_price > ?";

    long count = DB.sqlQuery(sql)
      .setParameter(1, 2)
      .mapToScalar(Long.class)
      .findOne();

    assertThat(count).isGreaterThan(0);
  }


  @Test
  public void findSingleAttribute_long() {

    ResetBasicData.reset();

    String sql = "select count(order_qty) from o_order_detail where unit_price > ?";

    long count = DB.sqlQuery(sql)
      .setParameter(1, 2)
      .mapToScalar(Long.class).findOne();

    assertThat(count).isGreaterThan(0);
  }

  @Test
  public void findSingleAttribute_OffsetDateTime() {

    ResetBasicData.reset();

    String sql = "select min(updtime) from o_order_detail where unit_price > ? and updtime is not null";

    OffsetDateTime minCreated = DB.sqlQuery(sql)
      .setParameter(1, 2)
      .mapToScalar(OffsetDateTime.class).findOne();

    assertThat(minCreated).isBefore(OffsetDateTime.now());
  }

  static class CustDto {

    long id;
    String name;
    String status;

    public CustDto(long id, String name, String status) {
      this.id = id;
      this.name = name;
      this.status = status;
    }
  }

  static class CustMapper implements RowMapper<CustDto> {

    @Override
    public CustDto map(ResultSet rset, int rowNum) throws SQLException {

      long id = rset.getLong(1);
      String name = rset.getString(2);
      String status = rset.getString(3);

      return new CustDto(id, name, status);
    }
  }

  private static final CustMapper CUST_MAPPER = new CustMapper();

  @Test
  public void findOne_mapper() {

    ResetBasicData.reset();

    String sql = "select id, name, status from o_customer where name = ?";

    CustDto rob = DB.sqlQuery(sql)
      .setParameter("Rob")
      .mapTo(CUST_MAPPER)
      .findOne();

    assertThat(rob.name).isEqualTo("Rob");
  }

  @Test
  public void findList_mapper() {

    ResetBasicData.reset();

    String sql = "select id, name, status from o_customer order by name desc";

    List<CustDto> dtos = DB.sqlQuery(sql)
      .mapTo(CUST_MAPPER)
      .findList();

    assertThat(dtos).isNotEmpty();
  }

  @Test
  public void findEachRow() {

    ResetBasicData.reset();

    String sql = "select id, name, status from o_customer order by name desc";

    AtomicLong count = new AtomicLong();

    DB.sqlQuery(sql)
      .findEachRow((resultSet, rowNum) -> {
        count.incrementAndGet();

        long id = resultSet.getLong(1);
        String name = resultSet.getString(2);

        System.out.println("rowNum:" + rowNum + " id:" + id + " name:" + name);
      });

    assertThat(count.get()).isGreaterThan(0);
  }

  @Test
  public void findOne_mapper_lambda() {

    ResetBasicData.reset();

    String sql = "select max(id) from o_customer where name != ?";

    long maxId = DB.sqlQuery(sql)
      .setParameter("Rob")
      .mapTo((resultSet, rowNum) -> resultSet.getLong(1))
      .findOne();

    assertThat(maxId).isGreaterThan(0);
  }

  @Test
  public void newline_replacedInLogsOnly() {

    ResetBasicData.reset();

    String sql = "select * -- \n from o_customer";
    SqlQuery sqlQuery = DB.sqlQuery(sql);

    List<SqlRow> list = sqlQuery.findList();
    assertThat(list).isNotEmpty();
  }

  @Test
  public void newLineLiteral_replacedInLogsOnly() {

    ResetBasicData.reset();

    String sql = "select 'hello\nthere' as hello from o_customer";
    SqlQuery sqlQuery = DB.sqlQuery(sql);

    List<SqlRow> list = sqlQuery.findList();
    assertThat(list).isNotEmpty();

    assertThat(list.get(0).getString("hello")).isEqualTo("hello\nthere");
  }

  @Test
  public void firstRowMaxRows() {

    ResetBasicData.reset();

    SqlQuery sqlQuery = DB.sqlQuery("Select * from o_order");
    sqlQuery.setFirstRow(3);
    sqlQuery.setMaxRows(10);

    LoggedSqlCollector.start();
    List<SqlRow> list = sqlQuery.findList();

    List<String> sql = LoggedSqlCollector.stop();

    if (isSqlServer()) {
      // FIXME: we should order by primary key ALWAYS (not by first column) when no
      // explicit order is specified. In postgres this leads to strange scrolling
      // artifacts.
      assertSql(sql.get(0)).contains("order by 1 offset 3 rows fetch next 10 rows only");
    } else if (isOracle()) {
      assertSql(sql.get(0)).contains("from o_order offset 3 rows fetch next 10 rows only");
    } else {
      assertSql(sql.get(0)).contains("Select * from o_order limit 10 offset 3; --bind()");
    }
    assertThat(list).isNotEmpty();
  }

  @Test
  public void firstRow() {

    if (isPostgres()) {

      ResetBasicData.reset();

      SqlQuery sqlQuery = DB.sqlQuery("Select * from o_order order by id");
      sqlQuery.setFirstRow(3);

      LoggedSqlCollector.start();
      sqlQuery.findList();
      List<String> sql = LoggedSqlCollector.stop();

      assertSql(sql.get(0)).contains("Select * from o_order order by id offset 3");
    }
  }

  @Test
  public void maxRows() {

    ResetBasicData.reset();

    SqlQuery sqlQuery = DB.sqlQuery("Select * from o_order order by id");
    sqlQuery.setMaxRows(10);

    LoggedSqlCollector.start();
    sqlQuery.findList();
    List<String> sql = LoggedSqlCollector.stop();

    if (isSqlServer()) {
      assertSql(sql.get(0)).contains("Select * from o_order order by id offset 0 rows fetch next 10 rows only;");
    } else if (isOracle()) {
      assertSql(sql.get(0)).contains("from o_order order by id fetch next 10 rows only;");
    } else {
      assertSql(sql.get(0)).contains("Select * from o_order order by id limit 10");
    }
  }

  @Test
  public void maxRows_withParam() {

    ResetBasicData.reset();

    resetAllMetrics();

    SqlQuery sqlQuery = DB.sqlQuery("select * from o_order where o_order.id > :id order by id")
      .setParameter("id", 3)
      .setMaxRows(10)
      .setLabel("findList-3-10");


    LoggedSqlCollector.start();
    sqlQuery.findList();
    List<String> sql = LoggedSqlCollector.stop();

    if (isSqlServer()) {
      assertSql(sql.get(0)).contains("select * from o_order where o_order.id > ? order by id offset 0 rows fetch next 10 rows only;");
    } else if (isOracle()) {
      assertSql(sql.get(0)).contains("order by id fetch next 10 rows only");
    } else {
      assertSql(sql.get(0)).contains("select * from o_order where o_order.id > ? order by id limit 10;");
    }

    assertThat(sqlMetrics()).isNotEmpty();
  }

  @Test
  public void findEachMaxRows() {

    ResetBasicData.reset();

    resetAllMetrics();

    SqlQuery sqlQuery = DB.sqlQuery("Select * from o_order")
      .setMaxRows(10)
      .setLabel("findEach-Max10Rows");

    LoggedSqlCollector.start();
    sqlQuery.findEach(bean -> bean.get("id"));
    List<String> sql = LoggedSqlCollector.stop();

    if (isSqlServer()) {
      assertSql(sql.get(0)).contains("offset 0 rows fetch next 10 rows only");
    } else if (isOracle()) {
      assertSql(sql.get(0)).contains("fetch next 10 rows only");
    } else {
      assertSql(sql.get(0)).contains("limit 10");
    }

    List<MetaTimedMetric> sqlMetrics = sqlMetrics();
    assertThat(sqlMetrics).hasSize(1);
    assertThat(sqlMetrics.get(0).getName()).isEqualTo("sql.query.findEach-Max10Rows");
  }

  @Test
  public void findEach() {

    ResetBasicData.reset();

    int expectedRows = Ebean.find(Order.class).findCount();

    final AtomicInteger count = new AtomicInteger();

    SqlQuery sqlQuery = DB.sqlQuery("select * from o_order");
    sqlQuery.findEach(bean -> count.incrementAndGet());

    assertEquals(expectedRows, count.get());
  }

  @Test
  public void findEachWhile() {

    ResetBasicData.reset();

    final AtomicInteger count = new AtomicInteger();

    SqlQuery sqlQuery = DB.sqlQuery("select * from o_order order by id");
    sqlQuery.findEachWhile(bean -> {
      count.incrementAndGet();
      Integer id = bean.getInteger("id");
      return id < 3;
    });

    assertEquals(3, count.get());
  }

}
