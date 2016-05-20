package com.avaje.ebeaninternal.server.querydefn;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.expression.DefaultExpressionFactory;
import com.avaje.tests.model.basic.Order;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

public class TestQueryLanguage extends BaseTestCase {

  @Test
  public void test() {

    DefaultOrmQuery<Order> q = check("find order join customer (id, name)");
    OrmQueryDetail detail = q.getDetail();
    OrmQueryProperties chunk = detail.getChunk("customer", false);
    Set<String> props = chunk.getIncluded();

    Assert.assertTrue(props.contains("id"));
    Assert.assertTrue(props.contains("name"));

    q = check("find order join customer(id, name)");
    detail = q.getDetail();
    chunk = detail.getChunk("customer", false);
    props = chunk.getIncluded();

    Assert.assertTrue(props.contains("id"));
    Assert.assertTrue(props.contains("name"));
    Assert.assertFalse(chunk.isCache());
    Assert.assertFalse(chunk.isReadOnly());

    q = check("find order join customer(+cache +readonly, id, name)");
    detail = q.getDetail();
    chunk = detail.getChunk("customer", false);
    props = chunk.getIncluded();
    Assert.assertTrue(props.contains("id"));
    Assert.assertTrue(props.contains("name"));
    Assert.assertTrue(chunk.isCache());
    Assert.assertTrue(chunk.isReadOnly());

    q = check("find order join customer(+cache +readonly,id,name)");
    detail = q.getDetail();
    chunk = detail.getChunk("customer", false);
    props = chunk.getIncluded();
    Assert.assertTrue(props.contains("id"));
    Assert.assertTrue(props.contains("name"));
    Assert.assertTrue(chunk.isCache());
    Assert.assertTrue(chunk.isReadOnly());

    q = check("find order(id,status) join customer(+cache +readonly,id,name)");
    detail = q.getDetail();
    chunk = detail.getChunk("customer", false);
    props = chunk.getIncluded();
    Assert.assertTrue(props.contains("id"));
    Assert.assertTrue(props.contains("name"));
    Assert.assertTrue(chunk.isCache());
    Assert.assertTrue(chunk.isReadOnly());

    chunk = detail.getChunk(null, false);
    props = chunk.getIncluded();
    Assert.assertTrue(props.contains("id"));
    Assert.assertTrue(props.contains("status"));
    Assert.assertFalse(props.contains("orderDate"));

    q = check("find order(id,status) join customer(+cache +readonly,id,name) where id > :minId order by status");
    detail = q.getDetail();
    chunk = detail.getChunk("customer", false);
    props = chunk.getIncluded();
    Assert.assertTrue(props.contains("id"));
    Assert.assertTrue(props.contains("name"));
    Assert.assertTrue(chunk.isCache());
    Assert.assertTrue(chunk.isReadOnly());

    String orderBy = q.getOrderBy().toStringFormat();
    Assert.assertEquals("status", orderBy);
  }

  private DefaultOrmQuery<Order> check(String q) {

    SpiEbeanServer server = (SpiEbeanServer)Ebean.getServer(null);

    OrmQueryDetailParser p = new OrmQueryDetailParser(q);
    p.parse();

    BeanDescriptor<Order> desc = server.getBeanDescriptor(Order.class);
    DefaultOrmQuery<Order> qry = new DefaultOrmQuery<Order>(desc, server,
        new DefaultExpressionFactory(false, false));
    p.assign(qry);

    return qry;
  }
}
