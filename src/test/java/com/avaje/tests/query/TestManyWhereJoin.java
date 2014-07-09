package com.avaje.tests.query;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestManyWhereJoin extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Query<Customer> query = Ebean.find(Customer.class).select("id,status")
	    // the where on a 'many' (like orders) requires an
	    // additional join and distinct which is independent
	    // of a fetch join (if there is a fetch join)
        .where().eq("orders.status", Order.Status.NEW)
        // .where().eq("orders.details.product.name", "Desk")
        .query();

    query.findList();
    String sql = query.getGeneratedSql();
    
    // select distinct t0.id c0, t0.status c1 
    // from o_customer t0 
    // join o_order u1 on u1.kcustomer_id = t0.id  
    // where u1.status = ? ; --bind(NEW)

    Assert.assertTrue(sql.indexOf("select distinct ") > -1);
    Assert.assertTrue(sql.indexOf("join o_order ") > -1);
    Assert.assertTrue(sql.indexOf(".status = ?") > -1);
    Assert.assertTrue(sql.contains("select distinct t0.id c0, t0.status c1 from o_customer t0 join o_order u1 on u1.kcustomer_id = t0.id  where u1.status = ?"));
  }
  
  @Test
  public void testWithFetchJoinAndWhere() {

    ResetBasicData.reset();

    Query<Customer> query = Ebean.find(Customer.class).select("id,status")
    	.fetch("orders")
	    // the where on a 'many' (like orders) requires an
	    // additional join and distinct which is independent
	    // of a fetch join (if there is a fetch join)
        .where().eq("orders.status", Order.Status.NEW)
        // .where().eq("orders.details.product.name", "Desk")
        .query();

    query.findList();
    String sql = query.getGeneratedSql();
    
    // select distinct t0.id c0, t0.status c1, 
    //        t1.id c2, t1.status c3, t1.order_date c4, t1.ship_date c5, t2.name c6, t1.cretime c7, t1.updtime c8, t1.kcustomer_id c9, t0.id 
    // from o_customer t0 
    // left outer join o_order t1 on t1.kcustomer_id = t0.id  
    // left outer join o_customer t2 on t2.id = t1.kcustomer_id  
    // join o_order u1 on u1.kcustomer_id = t0.id  
    // where t1.order_date is not null  and u1.status = ?
    // order by t0.id; --bind(NEW)

    Assert.assertTrue(sql.indexOf("select distinct t0.id c0, t0.status c1, t1.id c2, t1.status c3,") > -1);
    Assert.assertTrue(sql.indexOf("left outer join o_order t1 on ") > -1);
    Assert.assertTrue(sql.indexOf("join o_order u1 on ") > -1);
    Assert.assertTrue(sql.indexOf(" u1.status = ?") > -1);
  }
}
