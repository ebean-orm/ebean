package io.ebeaninternal.server.rawsql;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;
import io.ebeaninternal.server.rawsql.SpiRawSql.Sql;
import org.assertj.core.api.StrictAssertions;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;
import org.tests.model.rawsql.ERawSqlAggBean;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestRawSqlBuilder extends BaseTestCase {

  @Test
  public void testDeriveProperty() {
    StrictAssertions.assertThat(SpiRawSql.ColumnMapping.Column.derivePropertyName("item_total", "some_other")).isEqualTo("itemTotal");
    assertThat(SpiRawSql.ColumnMapping.Column.derivePropertyName(null, "some_other")).isEqualTo("someOther");
    assertThat(SpiRawSql.ColumnMapping.Column.derivePropertyName(null, "alias.some_other")).isEqualTo("someOther");
    assertThat(SpiRawSql.ColumnMapping.Column.derivePropertyName(null, "alias.someOther")).isEqualTo("someOther");
    assertThat(SpiRawSql.ColumnMapping.Column.derivePropertyName(null, "some")).isEqualTo("some");
    assertThat(SpiRawSql.ColumnMapping.Column.derivePropertyName(null, "someOther")).isEqualTo("someOther");
  }

  private Sql getSql(String sqlStatement) {
    RawSql r = RawSqlBuilder.parse(sqlStatement).create();
    return ((SpiRawSql)r).getSql();
  }

  @Test
  public void testSimple() {

    Sql sql = getSql("select id from t_cust");
    assertEquals("id", sql.getPreFrom());
    assertEquals("from t_cust", sql.getPreWhere());
    assertEquals("", sql.getPreHaving());
    assertNull(sql.getOrderBy());
  }

  @Test
  public void testWithNewLineCharacters() {

    Sql sql = getSql("select\n id from\n o_customer");

    assertEquals("id", sql.getPreFrom());
    assertEquals("from  o_customer", sql.getPreWhere());
    assertEquals("", sql.getPreHaving());
    assertNull(sql.getOrderBy());

    ResetBasicData.reset();

    RawSql rawSql = RawSqlBuilder.parse("select\n id from\n o_customer").create();

    Ebean.find(Customer.class)
      .setRawSql(rawSql)
      .findList();
  }

  @Test
  public void testWithWhere() {

    Sql sql = getSql("select id from t_cust where id > ?");

    assertEquals("id", sql.getPreFrom());
    assertEquals("from t_cust where id > ?", sql.getPreWhere());
    assertEquals("", sql.getPreHaving());
    assertNull(sql.getOrderBy());
  }

  @Test
  public void testWithOrder() {

    Sql sql = getSql("select id from t_cust where id > ? order by id desc");

    assertEquals("id", sql.getPreFrom());
    assertEquals("from t_cust where id > ?", sql.getPreWhere());
    assertEquals("", sql.getPreHaving());
    assertEquals("order by", sql.getOrderByPrefix());
    assertEquals("id desc", sql.getOrderBy());

    sql = getSql("select id from t_cust order by id desc");
    assertEquals("id", sql.getPreFrom());
    assertEquals("from t_cust", sql.getPreWhere());
    assertEquals("", sql.getPreHaving());
    assertEquals("id desc", sql.getOrderBy());

    sql = getSql("select id, sum(x) from t_cust where id > ? group by id order by id desc");
    assertEquals("id, sum(x)", sql.getPreFrom());
    assertEquals("from t_cust where id > ?", sql.getPreWhere());
    assertEquals("group by id", sql.getPreHaving());
    assertEquals("id desc", sql.getOrderBy());
  }

  @Test
  public void testWithHaving() {

    Sql sql = getSql("select id, sum(x) from t_cust where id > ? group by id having sum(x) > ? order by id desc");
    assertEquals("id, sum(x)", sql.getPreFrom());
    assertEquals("from t_cust where id > ?", sql.getPreWhere());
    assertEquals("group by id having sum(x) > ?", sql.getPreHaving());
    assertEquals("order by", sql.getOrderByPrefix());
    assertEquals("id desc", sql.getOrderBy());

    // no where
    sql = getSql("select id, sum(x) from t_cust group by id having sum(x) > ? order by id desc");
    assertEquals("id, sum(x)", sql.getPreFrom());
    assertEquals("from t_cust", sql.getPreWhere());
    assertEquals("group by id having sum(x) > ?", sql.getPreHaving());
    assertEquals("order by", sql.getOrderByPrefix());
    assertEquals("id desc", sql.getOrderBy());

    // no where, no order by
    sql = getSql("select id, sum(x) from t_cust group by id having sum(x) > ?");
    assertEquals("id, sum(x)", sql.getPreFrom());
    assertEquals("from t_cust", sql.getPreWhere());
    assertEquals("group by id having sum(x) > ?", sql.getPreHaving());
    assertNull(sql.getOrderBy());
    assertEquals("order by", sql.getOrderByPrefix());

    // no order by
    sql = getSql("select id, sum(x) from t_cust where id > ? group by id having sum(x) > ?");
    assertEquals("id, sum(x)", sql.getPreFrom());
    assertEquals("from t_cust where id > ?", sql.getPreWhere());
    assertEquals("group by id having sum(x) > ?", sql.getPreHaving());
    assertNull(sql.getOrderBy());
    assertEquals("order by", sql.getOrderByPrefix());
  }

  /**
   * test support for order siblings by ... Oracle syntax.
   */
  @Test
  public void testWithOrderSiblingsByName() {

    String s = "SELECT ID, DESCRIPTION, NAME, PARENT_ID FROM SOME_TABLE WHERE lower(NAME) like :name START WITH ID = :parentId CONNECT BY PRIOR ID = PARENT_ID order siblings by NAME";
    Sql sql = getSql(s);
    assertEquals("ID, DESCRIPTION, NAME, PARENT_ID", sql.getPreFrom());
    assertEquals("order siblings by", sql.getOrderByPrefix());
    assertEquals("NAME", sql.getOrderBy());
    assertEquals("FROM SOME_TABLE WHERE lower(NAME) like :name START WITH ID = :parentId CONNECT BY PRIOR ID = PARENT_ID", sql.getPreWhere());
  }


  @Test
  public void testWithAlias() {

    String rs = "select o.id, o.status, c.id, c.name, " +
      " d.id, d.order_qty, p.id, p.name " +
      "from o_order o join o_customer c on c.id = o.kcustomer_id " +
      "join o_order_detail d on d.order_id = o.id  " +
      "join o_product p on p.id = d.product_id  " +
      "where o.id <= :maxOrderId  and p.id = :productId " +
      "order by o.id, d.id asc";


    SpiRawSql rawSql = (SpiRawSql)RawSqlBuilder.parse(rs)
      .tableAliasMapping("c", "customer")
      .tableAliasMapping("d", "details")
      .tableAliasMapping("p", "details.product")
      .create();

    SpiRawSql.ColumnMapping columnMapping = rawSql.getColumnMapping();
    assertEquals(0, columnMapping.getIndexPosition("id"));
    assertEquals(1, columnMapping.getIndexPosition("status"));
    assertEquals(2, columnMapping.getIndexPosition("customer.id"));
    assertEquals(3, columnMapping.getIndexPosition("customer.name"));
    assertEquals(4, columnMapping.getIndexPosition("details.id"));
    assertEquals(5, columnMapping.getIndexPosition("details.orderQty"));
    assertEquals(6, columnMapping.getIndexPosition("details.product.id"));
    assertEquals(7, columnMapping.getIndexPosition("details.product.name"));

  }

  @Test
  public void testWithCoalesceFunction() {

    String rs = "select id, coalesce(status,'E') as status, " +
      " budgets.amount as  budget," +
      " COALESCE(month_sums.sum,0.0) as transaction_sum, " +
      " COALESCE(month_balances.balance,0.0) as balance, " +
      " COALESCE(month_sums.end_date,date_trunc('month',budgets.month),month_balances.end_date) as data_month" +
      " from o_order order by id asc";

    RawSqlBuilder builder = RawSqlBuilder.parse(rs);

    SpiRawSql rawSql = (SpiRawSql)builder.create();
    SpiRawSql.ColumnMapping columnMapping = rawSql.getColumnMapping();

    assertEquals(0, columnMapping.getIndexPosition("id"));
    assertEquals(1, columnMapping.getIndexPosition("status"));
    assertEquals(2, columnMapping.getIndexPosition("budget"));
    assertEquals(3, columnMapping.getIndexPosition("transactionSum"));
    assertEquals(4, columnMapping.getIndexPosition("balance"));
    assertEquals(5, columnMapping.getIndexPosition("dataMonth"));
  }

  @Test
  public void postgres_parse_withDateTruncCaseHaving() {

    if (!isPostgres()) {
      return;
    }

    ResetBasicData.reset();

    String sql = "select DATE_TRUNC('DAY', d.order_date) as day," +
      " count(*) as total," +
      " sum(case when d.status = 0 then 2 else 3 end) as scheduled," +
      " sum(case when d.status = 1 then 1 else 0 end) as completed" +
      " from o_order d" +
      " group by DATE_TRUNC('DAY', d.order_date)";

    SpiRawSql rawSql = (SpiRawSql)RawSqlBuilder.parse(sql).create();

    SpiRawSql.ColumnMapping columnMapping = rawSql.getColumnMapping();

    assertEquals(0, columnMapping.getIndexPosition("day"));
    assertEquals(1, columnMapping.getIndexPosition("total"));
    assertEquals(2, columnMapping.getIndexPosition("scheduled"));
    assertEquals(3, columnMapping.getIndexPosition("completed"));

    Query<ERawSqlAggBean> query = Ebean.find(ERawSqlAggBean.class)
      .setRawSql(rawSql)
      .having().gt("total", 2)
      .query();


    query.findList();

    String fullSql = query.getGeneratedSql();
    assertThat(fullSql).contains(" having count(*) > ?");

  }

}
