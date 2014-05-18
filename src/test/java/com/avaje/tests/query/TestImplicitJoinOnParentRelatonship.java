package com.avaje.tests.query;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestImplicitJoinOnParentRelatonship extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Query<Customer> query = Ebean.find(Customer.class)
        .select("id, name")
        .where().eq("orders.details.product.name", "Desk")
        .query();
    
    query.findList();

    String expectedSql = "select distinct t0.id c0, t0.name c1 from o_customer t0 join o_order u1 on u1.kcustomer_id = t0.id  join o_order_detail u2 on u2.order_id = u1.id  join o_product u3 on u3.id = u2.product_id  where u3.name = ? ";
    Assert.assertEquals(expectedSql, query.getGeneratedSql());
    
    // select distinct t0.id c0, t0.name c1 
    // from o_customer t0 
    // join o_order u1 on u1.kcustomer_id = t0.id  
    // join o_order_detail u2 on u2.order_id = u1.id 
    // join o_product u3 on u3.id = u2.product_id  
    // where u3.name = ? 
    
  }
  
  
  @Test
  public void testWithDisjunction() {

    ResetBasicData.reset();

    Query<Customer> query = Ebean.find(Customer.class)
        .select("id, name")
        .where().disjunction().eq("orders.details.product.name", "Desk").eq("id", 4).endJunction()
        .query();
    
    query.findList();
     
    String expectedSql = "select distinct t0.id c0, t0.name c1 from o_customer t0 left outer join o_order u1 on u1.kcustomer_id = t0.id  left outer join o_order_detail u2 on u2.order_id = u1.id  left outer join o_product u3 on u3.id = u2.product_id  where (u3.name = ?  or t0.id = ? ) ";
    Assert.assertEquals(expectedSql, query.getGeneratedSql());
    
    // select distinct t0.id c0, t0.name c1 
    // from o_customer t0 select distinct t0.id c0, t0.name c1 from o_customer t0 join o_order u1 on u1.kcustomer_id = t0.id  join o_order_detail u2 on u2.order_id = u1.id  join o_product u3 on u3.id = u2.product_id  where u3.name = ? 
    // left outer join o_order u1 on u1.kcustomer_id = t0.id  
    // left outer join o_order_detail u2 on u2.order_id = u1.id 
    // left outer join o_product u3 on u3.id = u2.product_id  
    // where (u3.name = ?  or t0.id = ? ) ; --bind(Desk,4)
    
  }

}
