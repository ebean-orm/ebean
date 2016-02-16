package com.avaje.tests.query;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.querydefn.OrmQueryDetail;
import com.avaje.tests.model.basic.Order;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestQueryParsing extends BaseTestCase {

  @Test
  public void test() {

    String oq = "find order join customer (id,  name) join customer.contacts join details (+query(4),+lazy(5))";
    Query<Order> q = Ebean.createQuery(Order.class, oq);
    checkQuery(q);

    String oq1 = "find order join customer (id,  name) join customer.contacts join details ( +query(4), +lazy(5) )";
    SpiQuery<?> q1 = (SpiQuery<?>) Ebean.createQuery(Order.class, oq1);
    checkQuery(q1);

    String oq2 = "find order join customer (id,  name) join customer.contacts join details ( +query(4), +lazy(5) , *)";
    SpiQuery<?> q2 = (SpiQuery<?>) Ebean.createQuery(Order.class, oq2);
    checkQuery(q2);

    String oq3 = "find order join customer (id,  name) join customer.contacts join details (+query(4),+lazy(5),*)";
    SpiQuery<?> q3 = (SpiQuery<?>) Ebean.createQuery(Order.class, oq3);
    checkQuery(q3);

    String oq4 = "find order join customer (id,  name) join customer.contacts join details (+query(4) +lazy(5) *)";
    SpiQuery<?> q4 = (SpiQuery<?>) Ebean.createQuery(Order.class, oq4);
    checkQuery(q4);

  }

  private void checkQuery(Query<?> q) {

    SpiQuery<?> sq = (SpiQuery<?>) q;
    OrmQueryDetail detail = sq.getDetail();
    assertTrue(detail.getChunk(null, false).allProperties());

    assertNotNull(detail.getChunk("customer", false));
    assertFalse(detail.getChunk("customer", false).isQueryFetch());
    assertFalse(detail.getChunk("customer", false).isLazyFetch());
    assertFalse(detail.getChunk("customer", false).allProperties());
    assertEquals("id,  name",detail.getChunk("customer", false).getProperties());

    assertNotNull(detail.getChunk("customer.contacts", false));
    assertFalse(detail.getChunk("customer.contacts", false).isQueryFetch());
    assertFalse(detail.getChunk("customer.contacts", false).isLazyFetch());
    assertTrue(detail.getChunk("customer.contacts", false).allProperties());

    assertNotNull(detail.getChunk("details", false));
    assertTrue(detail.getChunk("details", false).isQueryFetch());
    assertTrue(detail.getChunk("details", false).isLazyFetch());
    assertEquals(4, detail.getChunk("details", false).getQueryFetchBatch());
    assertEquals(5, detail.getChunk("details", false).getLazyFetchBatch());
    assertTrue(detail.getChunk("details", false).allProperties());

  }

}
