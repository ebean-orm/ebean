package com.avaje.ebean;

import org.junit.Assert;

import com.avaje.ebean.RawSql.Sql;

public class TestRawSqlBuilder extends BaseTestCase {

    public void testSimple() {
        
        RawSqlBuilder r  = RawSqlBuilder.parse("select id from t_cust");
        Sql sql = r.getSql();
        Assert.assertEquals("id", sql.getPreFrom());
        Assert.assertEquals("from t_cust", sql.getPreWhere());
        Assert.assertEquals("", sql.getPreHaving());
        Assert.assertNull(sql.getOrderBy());
        
    }
    
    public void testWithWhere() {
        
        RawSqlBuilder r  = RawSqlBuilder.parse("select id from t_cust where id > ?");
        Sql sql = r.getSql();
        Assert.assertEquals("id", sql.getPreFrom());
        Assert.assertEquals("from t_cust where id > ?", sql.getPreWhere());
        Assert.assertEquals("", sql.getPreHaving());
        Assert.assertNull(sql.getOrderBy());
    }
    
    public void testWithOrder() {
        
        RawSqlBuilder r  = RawSqlBuilder.parse("select id from t_cust where id > ? order by id desc");
        Sql sql = r.getSql();
        Assert.assertEquals("id", sql.getPreFrom());
        Assert.assertEquals("from t_cust where id > ?", sql.getPreWhere());
        Assert.assertEquals("", sql.getPreHaving());
        Assert.assertEquals("id desc", sql.getOrderBy());
        
        r  = RawSqlBuilder.parse("select id from t_cust order by id desc");
        sql = r.getSql();
        Assert.assertEquals("id", sql.getPreFrom());
        Assert.assertEquals("from t_cust", sql.getPreWhere());
        Assert.assertEquals("", sql.getPreHaving());
        Assert.assertEquals("id desc", sql.getOrderBy());
        
        r  = RawSqlBuilder.parse("select id, sum(x) from t_cust where id > ? group by id order by id desc");
        sql = r.getSql();
        Assert.assertEquals("id, sum(x)", sql.getPreFrom());
        Assert.assertEquals("from t_cust where id > ?", sql.getPreWhere());
        Assert.assertEquals("group by id", sql.getPreHaving());
        Assert.assertEquals("id desc", sql.getOrderBy());
    }
    
    public void testWithHaving() {
        
        RawSqlBuilder r  = RawSqlBuilder.parse("select id, sum(x) from t_cust where id > ? group by id having sum(x) > ? order by id desc");
        Sql sql = r.getSql();
        Assert.assertEquals("id, sum(x)", sql.getPreFrom());
        Assert.assertEquals("from t_cust where id > ?", sql.getPreWhere());
        Assert.assertEquals("group by id having sum(x) > ?", sql.getPreHaving());
        Assert.assertEquals("id desc", sql.getOrderBy());
        
        // no where
        r  = RawSqlBuilder.parse("select id, sum(x) from t_cust group by id having sum(x) > ? order by id desc");
        sql = r.getSql();
        Assert.assertEquals("id, sum(x)", sql.getPreFrom());
        Assert.assertEquals("from t_cust", sql.getPreWhere());
        Assert.assertEquals("group by id having sum(x) > ?", sql.getPreHaving());
        Assert.assertEquals("id desc", sql.getOrderBy());
        
        // no where, no order by
        r  = RawSqlBuilder.parse("select id, sum(x) from t_cust group by id having sum(x) > ?");
        sql = r.getSql();
        Assert.assertEquals("id, sum(x)", sql.getPreFrom());
        Assert.assertEquals("from t_cust", sql.getPreWhere());
        Assert.assertEquals("group by id having sum(x) > ?", sql.getPreHaving());
        Assert.assertNull(sql.getOrderBy());
        
        // no order by
        r  = RawSqlBuilder.parse("select id, sum(x) from t_cust where id > ? group by id having sum(x) > ?");
        sql = r.getSql();
        Assert.assertEquals("id, sum(x)", sql.getPreFrom());
        Assert.assertEquals("from t_cust where id > ?", sql.getPreWhere());
        Assert.assertEquals("group by id having sum(x) > ?", sql.getPreHaving());
        Assert.assertNull(sql.getOrderBy());
    }
}
