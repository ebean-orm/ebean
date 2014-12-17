package com.avaje.ebean;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.RawSql.Sql;

public class TestRawSqlBuilder extends BaseTestCase {

  @Test
  public void testSimple() {

    RawSqlBuilder r = RawSqlBuilder.parse("select id from t_cust");
    Sql sql = r.getSql();
    Assert.assertEquals("id", sql.getPreFrom());
    Assert.assertEquals("from t_cust", sql.getPreWhere());
    Assert.assertEquals("", sql.getPreHaving());
    Assert.assertNull(sql.getOrderBy());

  }

  @Test
  public void testWithWhere() {

    RawSqlBuilder r = RawSqlBuilder.parse("select id from t_cust where id > ?");
    Sql sql = r.getSql();
    Assert.assertEquals("id", sql.getPreFrom());
    Assert.assertEquals("from t_cust where id > ?", sql.getPreWhere());
    Assert.assertEquals("", sql.getPreHaving());
    Assert.assertNull(sql.getOrderBy());
  }

  @Test
  public void testWithOrder() {

    RawSqlBuilder r = RawSqlBuilder.parse("select id from t_cust where id > ? order by id desc");
    Sql sql = r.getSql();
    Assert.assertEquals("id", sql.getPreFrom());
    Assert.assertEquals("from t_cust where id > ?", sql.getPreWhere());
    Assert.assertEquals("", sql.getPreHaving());
    Assert.assertEquals("order by", sql.getOrderByPrefix());
    Assert.assertEquals("id desc", sql.getOrderBy());

    r = RawSqlBuilder.parse("select id from t_cust order by id desc");
    sql = r.getSql();
    Assert.assertEquals("id", sql.getPreFrom());
    Assert.assertEquals("from t_cust", sql.getPreWhere());
    Assert.assertEquals("", sql.getPreHaving());
    Assert.assertEquals("id desc", sql.getOrderBy());

    r = RawSqlBuilder
        .parse("select id, sum(x) from t_cust where id > ? group by id order by id desc");
    sql = r.getSql();
    Assert.assertEquals("id, sum(x)", sql.getPreFrom());
    Assert.assertEquals("from t_cust where id > ?", sql.getPreWhere());
    Assert.assertEquals("group by id", sql.getPreHaving());
    Assert.assertEquals("id desc", sql.getOrderBy());
  }

  @Test
  public void testWithHaving() {

    RawSqlBuilder r = RawSqlBuilder
        .parse("select id, sum(x) from t_cust where id > ? group by id having sum(x) > ? order by id desc");
    Sql sql = r.getSql();
    Assert.assertEquals("id, sum(x)", sql.getPreFrom());
    Assert.assertEquals("from t_cust where id > ?", sql.getPreWhere());
    Assert.assertEquals("group by id having sum(x) > ?", sql.getPreHaving());
    Assert.assertEquals("order by", sql.getOrderByPrefix());
    Assert.assertEquals("id desc", sql.getOrderBy());

    // no where
    r = RawSqlBuilder
        .parse("select id, sum(x) from t_cust group by id having sum(x) > ? order by id desc");
    sql = r.getSql();
    Assert.assertEquals("id, sum(x)", sql.getPreFrom());
    Assert.assertEquals("from t_cust", sql.getPreWhere());
    Assert.assertEquals("group by id having sum(x) > ?", sql.getPreHaving());
    Assert.assertEquals("order by", sql.getOrderByPrefix());
    Assert.assertEquals("id desc", sql.getOrderBy());

    // no where, no order by
    r = RawSqlBuilder.parse("select id, sum(x) from t_cust group by id having sum(x) > ?");
    sql = r.getSql();
    Assert.assertEquals("id, sum(x)", sql.getPreFrom());
    Assert.assertEquals("from t_cust", sql.getPreWhere());
    Assert.assertEquals("group by id having sum(x) > ?", sql.getPreHaving());
    Assert.assertNull(sql.getOrderBy());
    Assert.assertEquals("order by", sql.getOrderByPrefix());

    // no order by
    r = RawSqlBuilder
        .parse("select id, sum(x) from t_cust where id > ? group by id having sum(x) > ?");
    sql = r.getSql();
    Assert.assertEquals("id, sum(x)", sql.getPreFrom());
    Assert.assertEquals("from t_cust where id > ?", sql.getPreWhere());
    Assert.assertEquals("group by id having sum(x) > ?", sql.getPreHaving());
    Assert.assertNull(sql.getOrderBy());
    Assert.assertEquals("order by", sql.getOrderByPrefix());
  }

  /**
   * test support for order siblings by ... Oracle syntax.
   */
  @Test
  public void testWithOrderSiblingsByName() {

    String s =  "SELECT ID, DESCRIPTION, NAME, PARENT_ID FROM SOME_TABLE WHERE lower(NAME) like :name START WITH ID = :parentId CONNECT BY PRIOR ID = PARENT_ID order siblings by NAME";

    RawSql rawSql = RawSqlBuilder.parse(s).create();

    Sql sql = rawSql.getSql();
    Assert.assertEquals("ID, DESCRIPTION, NAME, PARENT_ID", sql.getPreFrom());
    Assert.assertEquals("order siblings by", sql.getOrderByPrefix());
    Assert.assertEquals("NAME", sql.getOrderBy());
    Assert.assertEquals("FROM SOME_TABLE WHERE lower(NAME) like :name START WITH ID = :parentId CONNECT BY PRIOR ID = PARENT_ID", sql.getPreWhere());

  }
}
