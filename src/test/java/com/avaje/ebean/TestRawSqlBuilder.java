package com.avaje.ebean;

import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;
import org.junit.Test;

import com.avaje.ebean.RawSql.Sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestRawSqlBuilder extends BaseTestCase {

  @Test
  public void testSimple() {

    RawSqlBuilder r = RawSqlBuilder.parse("select id from t_cust");
    Sql sql = r.getSql();
    assertEquals("id", sql.getPreFrom());
    assertEquals("from t_cust", sql.getPreWhere());
    assertEquals("", sql.getPreHaving());
    assertNull(sql.getOrderBy());
  }

  @Test
  public void testWithNewLineCharacters() {

    RawSqlBuilder r = RawSqlBuilder.parse("select\n id from\n o_customer");
    Sql sql = r.getSql();

    assertEquals("id", sql.getPreFrom());
    assertEquals("from  o_customer", sql.getPreWhere());
    assertEquals("", sql.getPreHaving());
    assertNull(sql.getOrderBy());

    ResetBasicData.reset();

    RawSql rawSql = r.create();

    Ebean.find(Customer.class)
        .setRawSql(rawSql)
        .findList();
  }

  @Test
  public void testWithWhere() {

    RawSqlBuilder r = RawSqlBuilder.parse("select id from t_cust where id > ?");
    Sql sql = r.getSql();
    assertEquals("id", sql.getPreFrom());
    assertEquals("from t_cust where id > ?", sql.getPreWhere());
    assertEquals("", sql.getPreHaving());
    assertNull(sql.getOrderBy());
  }

  @Test
  public void testWithOrder() {

    RawSqlBuilder r = RawSqlBuilder.parse("select id from t_cust where id > ? order by id desc");
    Sql sql = r.getSql();
    assertEquals("id", sql.getPreFrom());
    assertEquals("from t_cust where id > ?", sql.getPreWhere());
    assertEquals("", sql.getPreHaving());
    assertEquals("order by", sql.getOrderByPrefix());
    assertEquals("id desc", sql.getOrderBy());

    r = RawSqlBuilder.parse("select id from t_cust order by id desc");
    sql = r.getSql();
    assertEquals("id", sql.getPreFrom());
    assertEquals("from t_cust", sql.getPreWhere());
    assertEquals("", sql.getPreHaving());
    assertEquals("id desc", sql.getOrderBy());

    r = RawSqlBuilder
        .parse("select id, sum(x) from t_cust where id > ? group by id order by id desc");
    sql = r.getSql();
    assertEquals("id, sum(x)", sql.getPreFrom());
    assertEquals("from t_cust where id > ?", sql.getPreWhere());
    assertEquals("group by id", sql.getPreHaving());
    assertEquals("id desc", sql.getOrderBy());
  }

  @Test
  public void testWithHaving() {

    RawSqlBuilder r = RawSqlBuilder
        .parse("select id, sum(x) from t_cust where id > ? group by id having sum(x) > ? order by id desc");
    Sql sql = r.getSql();
    assertEquals("id, sum(x)", sql.getPreFrom());
    assertEquals("from t_cust where id > ?", sql.getPreWhere());
    assertEquals("group by id having sum(x) > ?", sql.getPreHaving());
    assertEquals("order by", sql.getOrderByPrefix());
    assertEquals("id desc", sql.getOrderBy());

    // no where
    r = RawSqlBuilder
        .parse("select id, sum(x) from t_cust group by id having sum(x) > ? order by id desc");
    sql = r.getSql();
    assertEquals("id, sum(x)", sql.getPreFrom());
    assertEquals("from t_cust", sql.getPreWhere());
    assertEquals("group by id having sum(x) > ?", sql.getPreHaving());
    assertEquals("order by", sql.getOrderByPrefix());
    assertEquals("id desc", sql.getOrderBy());

    // no where, no order by
    r = RawSqlBuilder.parse("select id, sum(x) from t_cust group by id having sum(x) > ?");
    sql = r.getSql();
    assertEquals("id, sum(x)", sql.getPreFrom());
    assertEquals("from t_cust", sql.getPreWhere());
    assertEquals("group by id having sum(x) > ?", sql.getPreHaving());
    assertNull(sql.getOrderBy());
    assertEquals("order by", sql.getOrderByPrefix());

    // no order by
    r = RawSqlBuilder
        .parse("select id, sum(x) from t_cust where id > ? group by id having sum(x) > ?");
    sql = r.getSql();
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

    String s =  "SELECT ID, DESCRIPTION, NAME, PARENT_ID FROM SOME_TABLE WHERE lower(NAME) like :name START WITH ID = :parentId CONNECT BY PRIOR ID = PARENT_ID order siblings by NAME";

    RawSql rawSql = RawSqlBuilder.parse(s).create();

    Sql sql = rawSql.getSql();
    assertEquals("ID, DESCRIPTION, NAME, PARENT_ID", sql.getPreFrom());
    assertEquals("order siblings by", sql.getOrderByPrefix());
    assertEquals("NAME", sql.getOrderBy());
    assertEquals("FROM SOME_TABLE WHERE lower(NAME) like :name START WITH ID = :parentId CONNECT BY PRIOR ID = PARENT_ID", sql.getPreWhere());

  }



  @Test
  public void testWithAlias() {

    String rs = "select o.id, o.status, c.id, c.name, "+
            " d.id, d.order_qty, p.id, p.name " +
            "from o_order o join o_customer c on c.id = o.kcustomer_id " +
            "join o_order_detail d on d.order_id = o.id  " +
            "join o_product p on p.id = d.product_id  " +
            "where o.id <= :maxOrderId  and p.id = :productId "+
            "order by o.id, d.id asc";


    RawSql rawSql = RawSqlBuilder.parse(rs)
            .tableAliasMapping("c", "customer")
            .tableAliasMapping("d", "details")
            .tableAliasMapping("p", "details.product")
            .create();

    RawSql.ColumnMapping columnMapping = rawSql.getColumnMapping();
    assertEquals(0, columnMapping.getIndexPosition("id"));
    assertEquals(1, columnMapping.getIndexPosition("status"));
    assertEquals(2, columnMapping.getIndexPosition("customer.id"));
    assertEquals(3, columnMapping.getIndexPosition("customer.name"));
    assertEquals(4, columnMapping.getIndexPosition("details.id"));
    assertEquals(5, columnMapping.getIndexPosition("details.orderQty"));
    assertEquals(6, columnMapping.getIndexPosition("details.product.id"));
    assertEquals(7, columnMapping.getIndexPosition("details.product.name"));

  }

}
