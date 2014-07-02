package com.avaje.ebeaninternal.server.querydefn;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebeaninternal.server.expression.DefaultExpressionFactory;
import com.avaje.tests.model.basic.Order;

public class TestQueryLanguage extends BaseTestCase {

  @Test
  public void test() {

    DefaultOrmQuery<Order> q = check("find order join customer (id, name)");
    OrmQueryDetail detail = q.getDetail();
    OrmQueryProperties chunk = detail.getChunk("customer", false);
    Set<String> props = chunk.getAllIncludedProperties();

    Assert.assertTrue(props.contains("id"));
    Assert.assertTrue(props.contains("name"));

    q = check("find order join customer(id, name)");
    detail = q.getDetail();
    chunk = detail.getChunk("customer", false);
    props = chunk.getAllIncludedProperties();

    Assert.assertTrue(props.contains("id"));
    Assert.assertTrue(props.contains("name"));
    Assert.assertFalse(chunk.isCache());
    Assert.assertFalse(chunk.isReadOnly());

    q = check("find order join customer(+cache +readonly, id, name)");
    detail = q.getDetail();
    chunk = detail.getChunk("customer", false);
    props = chunk.getAllIncludedProperties();
    Assert.assertTrue(props.contains("id"));
    Assert.assertTrue(props.contains("name"));
    Assert.assertTrue(chunk.isCache());
    Assert.assertTrue(chunk.isReadOnly());

    q = check("find order join customer(+cache +readonly,id,name)");
    detail = q.getDetail();
    chunk = detail.getChunk("customer", false);
    props = chunk.getAllIncludedProperties();
    Assert.assertTrue(props.contains("id"));
    Assert.assertTrue(props.contains("name"));
    Assert.assertTrue(chunk.isCache());
    Assert.assertTrue(chunk.isReadOnly());

    q = check("find order(id,status) join customer(+cache +readonly,id,name)");
    detail = q.getDetail();
    chunk = detail.getChunk("customer", false);
    props = chunk.getAllIncludedProperties();
    Assert.assertTrue(props.contains("id"));
    Assert.assertTrue(props.contains("name"));
    Assert.assertTrue(chunk.isCache());
    Assert.assertTrue(chunk.isReadOnly());

    chunk = detail.getChunk(null, false);
    props = chunk.getAllIncludedProperties();
    Assert.assertTrue(props.contains("id"));
    Assert.assertTrue(props.contains("status"));
    Assert.assertFalse(props.contains("orderDate"));

    q = check("find order(id,status) join customer(+cache +readonly,id,name) where id > :minId order by status");
    detail = q.getDetail();
    chunk = detail.getChunk("customer", false);
    props = chunk.getAllIncludedProperties();
    Assert.assertTrue(props.contains("id"));
    Assert.assertTrue(props.contains("name"));
    Assert.assertTrue(chunk.isCache());
    Assert.assertTrue(chunk.isReadOnly());

    String orderBy = q.getOrderBy().toStringFormat();
    Assert.assertEquals("status", orderBy);
  }

  private DefaultOrmQuery<Order> check(String q) {

    EbeanServer server = Ebean.getServer(null);

    OrmQueryDetailParser p = new OrmQueryDetailParser(q);
    p.parse();
    DefaultOrmQuery<Order> qry = new DefaultOrmQuery<Order>(Order.class, server,
        new DefaultExpressionFactory(), (String) null);
    p.assign(qry);

    return qry;
  }
}
