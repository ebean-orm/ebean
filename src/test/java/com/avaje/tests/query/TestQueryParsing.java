package com.avaje.tests.query;

import junit.framework.TestCase;

import org.junit.Assert;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.querydefn.OrmQueryDetail;
import com.avaje.tests.model.basic.Order;

public class TestQueryParsing extends TestCase {

    public void test() {
        
        String oq = "find order join customer join customer.contacts join details (+query(4),+lazy(5))";
        Query<Order> q = Ebean.createQuery(Order.class, oq);    
        checkQuery(q);

        String oq1 = "find order join customer join customer.contacts join details ( +query(4), +lazy(5) )";
        SpiQuery<?> q1 = (SpiQuery<?>)Ebean.createQuery(Order.class, oq1);    
        checkQuery(q1);

        String oq2 = "find order join customer join customer.contacts join details ( +query(4), +lazy(5) , *)";
        SpiQuery<?> q2 = (SpiQuery<?>)Ebean.createQuery(Order.class, oq2);    
        checkQuery(q2);
        
        String oq3 = "find order join customer join customer.contacts join details (+query(4),+lazy(5),*)";
        SpiQuery<?> q3 = (SpiQuery<?>)Ebean.createQuery(Order.class, oq3);    
        checkQuery(q3);

        String oq4 = "find order join customer join customer.contacts join details (+query(4) +lazy(5) *)";
        SpiQuery<?> q4 = (SpiQuery<?>)Ebean.createQuery(Order.class, oq4);    
        checkQuery(q4);

    }
    
    private void checkQuery(Query<?> q){
            
        
        SpiQuery<?> sq = (SpiQuery<?>)q;
        OrmQueryDetail detail = sq.getDetail();
        
        Assert.assertNotNull(detail.getChunk("customer", false));
        Assert.assertFalse(detail.getChunk("customer", false).isQueryFetch());
        Assert.assertFalse(detail.getChunk("customer", false).isLazyFetch());
        
        Assert.assertNotNull(detail.getChunk("customer.contacts", false));
        Assert.assertFalse(detail.getChunk("customer.contacts", false).isQueryFetch());
        Assert.assertFalse(detail.getChunk("customer.contacts", false).isLazyFetch());

        Assert.assertNotNull(detail.getChunk("details", false));
        Assert.assertTrue(detail.getChunk("details", false).isQueryFetch());
        Assert.assertTrue(detail.getChunk("details", false).isLazyFetch());

        Assert.assertEquals(4, detail.getChunk("details", false).getQueryFetchBatch());
        Assert.assertEquals(5, detail.getChunk("details", false).getLazyFetchBatch());

        Assert.assertTrue(detail.getChunk("details", false).allProperties());
        
    }
    
}
