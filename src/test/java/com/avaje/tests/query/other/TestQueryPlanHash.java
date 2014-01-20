package com.avaje.tests.query.other;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.Ebean;
import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.tests.model.basic.Order;

public class TestQueryPlanHash {

  @Test
  public void test() {
    

    HashQueryPlanBuilder builder1 = new HashQueryPlanBuilder();
    
    SpiQuery<Order> squery = (SpiQuery<Order>)Ebean.find(Order.class);
    squery.queryAutofetchHash(builder1);
    
    squery.where().in("id", 1,2,3);

    HashQueryPlanBuilder builder2 = new HashQueryPlanBuilder();
    squery.queryAutofetchHash(builder2);
    int q1BindHash = squery.queryBindHash();
    
    Assert.assertNotSame(builder1.build(), builder2.build());

    HashQueryPlanBuilder builder3 = new HashQueryPlanBuilder();
    SpiQuery<Order> squery2 = (SpiQuery<Order>)Ebean.find(Order.class);
    squery2.where().in("id", 2,2,3);

    squery2.queryAutofetchHash(builder3);
    int q2BindHash = squery2.queryBindHash();

    Assert.assertEquals(builder3.build(), builder2.build());    
 
    Assert.assertTrue(q1BindHash != q2BindHash);

    HashQueryPlanBuilder builder4 = new HashQueryPlanBuilder();
    SpiQuery<Order> squery4 = (SpiQuery<Order>)Ebean.find(Order.class);
    squery4.where().in("name", 2,2,3);

    squery4.queryAutofetchHash(builder4);
    int q4BindHash = squery4.queryBindHash();

    //different query plan
    Assert.assertTrue(!builder3.build().equals(builder4.build()));    
    
    //same bind hash
    Assert.assertTrue(q4BindHash == q2BindHash);

  }
  
}
