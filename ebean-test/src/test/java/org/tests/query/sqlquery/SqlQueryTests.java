package org.tests.query.sqlquery;

import io.ebean.*;
import io.ebean.xtest.BaseTestCase;
import io.ebean.xtest.ForPlatform;
import io.ebean.annotation.Platform;
import io.ebean.meta.MetaTimedMetric;
import io.ebean.test.LoggedSql;
import jakarta.persistence.NonUniqueResultException;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SqlQueryTests extends BaseTestCase {

  @ForPlatform(Platform.H2)
  @Test
  void selectBindNull_byPos() {
    String sql = "select nvl(cast(? as int), 42)";
    final Long val = DB.sqlQuery(sql)
      .setNullParameter(1, Types.INTEGER)
      .mapToScalar(Long.class)
      .findOne();

    assertThat(val).isEqualTo(42);
  }

  @ForPlatform(Platform.H2)
  @Test
  void selectBindNull_byName() {
    String sql = "select nvl(cast(:val as int), 42)";
    final Long val = DB.sqlQuery(sql)
      .setNullParameter("val", Types.INTEGER)
      .mapToScalar(Long.class)
      .findOne();

    assertThat(val).isEqualTo(42);
  }

  @ForPlatform(Platform.H2)
  @Test
  void selectBindNull_usingSetParameter_ByPosition() {
    String sql = "select nvl(cast(? as int), 42)";
    final Long val = DB.sqlQuery(sql)
      .setParameter(1, null)
      .mapToScalar(Long.class)
      .findOne();

    assertThat(val).isEqualTo(42);
  }

  @ForPlatform(Platform.H2)
  @Test
  void selectBindNull_usingSetParameter_byName() {
    String sql = "select nvl(cast(:val as int), 42)";
    final Long val = DB.sqlQuery(sql)
      .setParameter("val", null)
      .mapToScalar(Long.class)
      .findOne();

    assertThat(val).isEqualTo(42);
  }

  @ForPlatform(Platform.H2)
  @Test
  void selectFunction() {
    String sql = "select length(?)";
    final Long val = DB.sqlQuery(sql).setParameter("NotVeryLong").mapToScalar(Long.class).findOne();
    assertThat(val).isEqualTo(11);

    String sql2 = "select length(:val)";
    final Long val2 = DB.sqlQuery(sql2).setParameter("val", "NotVeryLong").mapToScalar(Long.class).findOne();
    assertThat(val2).isEqualTo(11);
  }

  @Test
  void findSingleAttribute_whenMultipleRows_expect_NonUniqueResultException() {
    ResetBasicData.reset();
    String sql = "select id from o_order order by id desc";

    assertThatThrownBy(() -> {
      DB.sqlQuery(sql)
        .mapToScalar(Long.class)
        .findOne();
    }).isInstanceOf(NonUniqueResultException.class)
      .hasMessageContaining("Got more than 1 result for findSingleAttribute");
  }

  @Test
  void findSingleAttributeList_decimal() {
    ResetBasicData.reset();

    String sql = "select (unit_price * order_qty) from o_order_detail where unit_price > ? order by (unit_price * order_qty) desc";

    List<BigDecimal> lineAmounts = DB.sqlQuery(sql)
      .setParameter(3)
      .mapToScalar(BigDecimal.class)
      .findList();

    assertThat(lineAmounts).isNotEmpty();
  }

  @Test
  void findSingleAttributeEach_decimal() {
    ResetBasicData.reset();

    String sql = "select (unit_price * order_qty) from o_order_detail where unit_price > ? order by (unit_price * order_qty) desc";

    AtomicLong counter = new AtomicLong();
    AtomicLong inc = new AtomicLong();

    DB.sqlQuery(sql)
      .setParameter(3)
      .mapToScalar(BigDecimal.class)
      .findEach(val -> {
        counter.incrementAndGet();
        inc.addAndGet(val.longValue());
      });

    assertThat(inc.get()).isGreaterThan(counter.get());
    assertThat(counter.get()).isGreaterThan(0);
  }

  @Test
  void findSingleDecimal() {
    ResetBasicData.reset();

    String sql = "select max(unit_price) from o_order_detail where order_qty > ?";

    BigDecimal maxPrice = DB.sqlQuery(sql)
      .setParameter(1, 2)
      .mapToScalar(BigDecimal.class)
      .findOne();

    assertThat(maxPrice).isNotNull();
  }

  @Test
  void findSingleAttribute_BigDecimal() {
    ResetBasicData.reset();

    String sql = "select max(unit_price) from o_order_detail where order_qty > ?";

    BigDecimal maxPrice = DB.sqlQuery(sql)
      .setParameter(2)
      .mapToScalar(BigDecimal.class)
      .findOne();

    assertThat(maxPrice).isNotNull();
  }

  @Test
  void findSingleLong() {
    ResetBasicData.reset();

    String sql = "select count(order_qty) from o_order_detail where unit_price > ?";
    long count = DB.sqlQuery(sql)
      .setParameter(1, 2)
      .mapToScalar(Long.class)
      .findOne();

    assertThat(count).isGreaterThan(0);
  }

  @Test
  void findSingleAttribute_long() {
    ResetBasicData.reset();

    String sql = "select count(order_qty) from o_order_detail where unit_price > ?";
    long count = DB.sqlQuery(sql)
      .setParameter(1, 2)
      .mapToScalar(Long.class).findOne();

    assertThat(count).isGreaterThan(0);
  }

  @Test
  void findSingleAttribute_OffsetDateTime() {
    ResetBasicData.reset();

    String sql = "select min(updtime) from o_order_detail where unit_price > ? and updtime is not null";
    OffsetDateTime minCreated = DB.sqlQuery(sql)
      .setParameter(1, 2)
      .mapToScalar(OffsetDateTime.class).findOne();

    assertThat(minCreated).isBefore(OffsetDateTime.now());
  }

  @Test
  void typeQuery_usingTransaction() throws SQLException {
    ResetBasicData.reset();
    boolean h2 = isH2();

    String sql = "select max(unit_price) from o_order_detail where order_qty > ?";
    try (Transaction transaction = DB.createTransaction()) {
      String h2SessionId = h2 ? h2SessionId(transaction) : null;

      BigDecimal maxPrice = DB.sqlQuery(sql)
        .setParameter(1, 2)
        .mapToScalar(BigDecimal.class)
        .usingTransaction(transaction)
        .findOne();

      if (h2) {
        var result = DB.sqlQuery("select 'hello-'||session_id()")
          .mapToScalar(String.class)
          .usingTransaction(transaction)
          .findOne();
        assertThat(result).isEqualTo("hello-" + h2SessionId);
      }
      assertThat(maxPrice).isNotNull();
    }
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

  private String h2SessionId(Transaction transaction) throws SQLException {
    if (isH2()) {
      try (PreparedStatement stmt = transaction.connection().prepareStatement("select session_id()")) {
        try (ResultSet rset = stmt.executeQuery()) {
          if (rset.next()) {
            return rset.getString(1);
          }
        }
      }
    }
    return null;
  }

  @Test
  void queryUsingMaster() {
    ResetBasicData.reset();

    String sql = "select id, name, status from o_customer where name is not null";
    List<CustDto> custDtos = DB.sqlQuery(sql)
      .usingMaster()
      .mapTo(CUST_MAPPER)
      .findList();

    assertThat(custDtos).isNotEmpty();
  }

  @Test
  void queryUsingMaster_true() {
    queryUsingMasterAsParameter(true);
  }

  @Test
  void queryUsingMaster_false() {
    queryUsingMasterAsParameter(false);
  }

  void queryUsingMasterAsParameter(boolean useMaster) {
    ResetBasicData.reset();

    String sql = "select id, name, status from o_customer where name is not null";
    List<CustDto> custDtos = DB.sqlQuery(sql)
      .usingMaster(useMaster)
      .mapTo(CUST_MAPPER)
      .findList();

    assertThat(custDtos).isNotEmpty();
  }

  @Test
  void queryUsingConnection() throws SQLException {
    ResetBasicData.reset();
    boolean h2 = isH2();

    String sql = h2 ? "select id, name||session_id(), status from o_customer where name is not null"
      : "select id, name, status from o_customer where name is not null";

    try (Transaction txn = DB.createTransaction()) {
      String h2SessionId = h2 ? h2SessionId(txn) : null;

      AtomicInteger counter = new AtomicInteger();
      DB.sqlQuery(sql)
        .usingConnection(txn.connection())
        .mapTo(CUST_MAPPER)
        .findEach(custDto -> {
          counter.incrementAndGet();
          assertThat(custDto.name).isNotNull();
          if (h2) {
            assertThat(custDto.name).endsWith(h2SessionId);
          }
        });

      assertThat(counter.get()).isGreaterThan(0);
    }
  }

  @Test
  void queryUsingTransaction() throws SQLException {
    ResetBasicData.reset();
    boolean h2 = isH2();

    String sql = h2 ? "select id, name||session_id(), status from o_customer where name is not null"
      : "select id, name, status from o_customer where name is not null";

    try (Transaction txn = DB.createTransaction()) {
      String h2SessionId = h2 ? h2SessionId(txn) : null;

      AtomicInteger counter = new AtomicInteger();
      DB.sqlQuery(sql)
        .usingTransaction(txn)
        .mapTo(CUST_MAPPER)
        .findEach(custDto -> {
          counter.incrementAndGet();
          assertThat(custDto.name).isNotNull();
          if (h2) {
            assertThat(custDto.name).endsWith(h2SessionId);
          }
        });

      assertThat(counter.get()).isGreaterThan(0);
    }
  }

  @Test
  void mapperUsingTransaction() throws SQLException {
    ResetBasicData.reset();
    boolean h2 = isH2();
    String sql = h2 ? "select id, name||session_id() as name, status from o_customer where name is not null"
      : "select id, name, status from o_customer where name is not null";
    try (Transaction txn = DB.createTransaction()) {
      String h2SessionId = h2 ? h2SessionId(txn) : null;

      AtomicInteger counter = new AtomicInteger();
      DB.sqlQuery(sql)
        .mapTo(CUST_MAPPER)
        .usingTransaction(txn)
        .findEach(custDto -> {
          counter.incrementAndGet();
          assertThat(custDto.name).isNotNull();
          if (h2) {
            assertThat(custDto.name).endsWith(h2SessionId);
          }
        });

      assertThat(counter.get()).isGreaterThan(0);
    }
  }

  @Test
  void findEach_mapper() {
    ResetBasicData.reset();

    String sql = "select id, name, status from o_customer where name is not null";
    AtomicInteger counter = new AtomicInteger();
    DB.sqlQuery(sql)
      .mapTo(CUST_MAPPER)
      .findEach(custDto -> {
        counter.incrementAndGet();
        assertThat(custDto.name).isNotNull();
      });

    assertThat(counter.get()).isGreaterThan(0);
  }

  @Test
  void findOne_mapper() {
    ResetBasicData.reset();

    String sql = "select id, name, status from o_customer where name = ?";
    CustDto rob = DB.sqlQuery(sql)
      .setParameter("Rob")
      .mapTo(CUST_MAPPER)
      .findOne();

    assertThat(rob.name).isEqualTo("Rob");
  }

  @Test
  void findOne_mapper_when_notUnique() {
    ResetBasicData.reset();

    String sql = "select id, name, status from o_customer order by name desc";
    assertThatThrownBy(() -> DB.sqlQuery(sql)
      .mapTo(CUST_MAPPER)
      .findOne())
      .isInstanceOf(NonUniqueResultException.class)
      .hasMessageContaining("Got more than 1 result for findOne");
  }

  @Test
  void findList_mapper() {
    ResetBasicData.reset();

    String sql = "select id, name, status from o_customer order by name desc";
    List<CustDto> dtos = DB.sqlQuery(sql)
      .mapTo(CUST_MAPPER)
      .findList();

    assertThat(dtos).isNotEmpty();
  }

  @Test
  void findEachRow() {
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
  void findOne_mapper_lambda() {
    ResetBasicData.reset();

    String sql = "select max(id) from o_customer where name != ?";
    long maxId = DB.sqlQuery(sql)
      .setParameter("Rob")
      .mapTo((resultSet, rowNum) -> resultSet.getLong(1))
      .findOne();

    assertThat(maxId).isGreaterThan(0);
  }

  @Test
  void newline_replacedInLogsOnly() {
    ResetBasicData.reset();

    String sql = "select * -- \n from o_customer";
    SqlQuery sqlQuery = DB.sqlQuery(sql);

    List<SqlRow> list = sqlQuery.findList();
    assertThat(list).isNotEmpty();
  }

  @Test
  void newLineLiteral_replacedInLogsOnly() {
    ResetBasicData.reset();

    String sql = "select 'hello\nthere' as hello from o_customer";
    SqlQuery sqlQuery = DB.sqlQuery(sql);

    List<SqlRow> list = sqlQuery.findList();
    assertThat(list).isNotEmpty();

    assertThat(list.get(0).getString("hello")).isEqualTo("hello\nthere");
  }

  @Test
  void firstRowMaxRows() {
    ResetBasicData.reset();

    SqlQuery sqlQuery = DB.sqlQuery("Select * from o_order");
    sqlQuery.setFirstRow(3);
    sqlQuery.setMaxRows(10);

    LoggedSql.start();
    List<SqlRow> list = sqlQuery.findList();

    List<String> sql = LoggedSql.stop();

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
  void firstRow() {
    if (isPostgresCompatible()) {

      ResetBasicData.reset();

      SqlQuery sqlQuery = DB.sqlQuery("Select * from o_order order by id");
      sqlQuery.setFirstRow(3);

      LoggedSql.start();
      sqlQuery.findList();
      List<String> sql = LoggedSql.stop();

      assertSql(sql.get(0)).contains("Select * from o_order order by id offset 3");
    }
  }

  @Test
  void maxRows() {
    ResetBasicData.reset();

    SqlQuery sqlQuery = DB.sqlQuery("Select * from o_order order by id");
    sqlQuery.setMaxRows(10);

    LoggedSql.start();
    sqlQuery.findList();
    List<String> sql = LoggedSql.stop();

    if (isSqlServer()) {
      assertSql(sql.get(0)).contains("Select * from o_order order by id offset 0 rows fetch next 10 rows only;");
    } else if (isOracle()) {
      assertSql(sql.get(0)).contains("from o_order order by id fetch next 10 rows only;");
    } else {
      assertSql(sql.get(0)).contains("Select * from o_order order by id limit 10");
    }
  }

  @Test
  void maxRows_withParam() {
    ResetBasicData.reset();

    resetAllMetrics();

    SqlQuery sqlQuery = DB.sqlQuery("select * from o_order where o_order.id > :id order by id")
      .setParameter("id", 3)
      .setMaxRows(10)
      .setLabel("findList-3-10");


    LoggedSql.start();
    sqlQuery.findList();
    List<String> sql = LoggedSql.stop();

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
  void findEachMaxRows() {
    ResetBasicData.reset();

    resetAllMetrics();

    SqlQuery sqlQuery = DB.sqlQuery("Select * from o_order")
      .setMaxRows(10)
      .setLabel("findEach-Max10Rows");

    LoggedSql.start();
    sqlQuery.findEach(bean -> bean.get("id"));
    List<String> sql = LoggedSql.stop();

    if (isSqlServer()) {
      assertSql(sql.get(0)).contains("offset 0 rows fetch next 10 rows only");
    } else if (isOracle()) {
      assertSql(sql.get(0)).contains("fetch next 10 rows only");
    } else {
      assertSql(sql.get(0)).contains("limit 10");
    }

    List<MetaTimedMetric> sqlMetrics = sqlMetrics();
    assertThat(sqlMetrics).hasSize(1);
    assertThat(sqlMetrics.get(0).name()).isEqualTo("sql.query.findEach-Max10Rows");
  }

  @Test
  void findEach() {
    ResetBasicData.reset();

    int expectedRows = DB.find(Order.class).findCount();

    final AtomicInteger count = new AtomicInteger();
    SqlQuery sqlQuery = DB.sqlQuery("select * from o_order");
    sqlQuery.findEach(bean -> count.incrementAndGet());

    assertEquals(expectedRows, count.get());
  }

  @Test
  void findEachWhile() {
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
