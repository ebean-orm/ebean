package com.avaje.tests.batchload;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.FetchConfig;
import com.avaje.ebean.Query;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestSecondaryQueries extends BaseTestCase {

  @Test
  public void testSecQueryOneToMany() {

    ResetBasicData.reset();

    Order testOrder = ResetBasicData.createOrderCustAndOrder("testSecQry10");
    Integer custId = testOrder.getCustomer().getId();

     Query<Customer> query = Ebean.find(Customer.class)
         .select("name")
         .fetch("contacts", "+query")
         .setId(custId);

    SpiQuery<?> spiQuery  = (SpiQuery<?>)query;
    spiQuery.setLogSecondaryQuery(true);
    
    Customer cust = query.findUnique();
    
    Assert.assertNotNull(cust);
    String generatedSql = query.getGeneratedSql();
    Assert.assertTrue(generatedSql.contains("from o_customer t0 where t0.id = ?"));
    
    List<SpiQuery<?>> loggedSecondaryQueries = spiQuery.getLoggedSecondaryQueries();
    Assert.assertEquals(1, loggedSecondaryQueries.size());
    
    SpiQuery<?> secondaryQuery = loggedSecondaryQueries.get(0);
    String secondarySql = secondaryQuery.getGeneratedSql();
    
    Assert.assertTrue(secondarySql.contains("from contact t0 where (t0.customer_id) in (?)"));
  }
  
  
  @Test
  public void testManyToOneWithManyPlusOneToMany() {

    ResetBasicData.reset();

    Query<Order> query = Ebean.find(Order.class)
        .select("status")
        .fetch("customer", "name, status", new FetchConfig().query())
        .fetch("customer.contacts")
        .fetch("details", new FetchConfig().query())
        .where().eq("status", Order.Status.NEW)
        .query();

//  .fetch("customer", "+query name, status")
//  .fetch("details", "+query(10)")
    
    SpiQuery<?> spiQuery  = (SpiQuery<?>)query;
    spiQuery.setLogSecondaryQuery(true);
    
    List<Order> list = query.findList();
    Assert.assertTrue(list.size() > 0);
    for (Order order : list) {
      order.getCustomer().getStatus();
    }
    
    
    String generatedSql = spiQuery.getGeneratedSql();
    //select t0.id c0, t0.status c1, t0.kcustomer_id c2 from o_order t0 where t0.status = ? ; --bind(NEW)
    Assert.assertEquals("select t0.id c0, t0.status c1, t0.kcustomer_id c2 from o_order t0 where t0.status = ? ", generatedSql);

    
    List<SpiQuery<?>> secondaryQueries = spiQuery.getLoggedSecondaryQueries();
    Assert.assertEquals(2, secondaryQueries.size());
    
    SpiQuery<?> custSecondaryQuery = secondaryQueries.get(0);
    String custSecondarySql = custSecondaryQuery.getGeneratedSql();
    
    // select t0.id c0, t0.name c1, t0.status c2, 
    //        t1.id c3, t1.first_name c4, t1.last_name c5, t1.phone c6, t1.mobile c7, t1.email c8, t1.cretime c9, t1.updtime c10, t1.customer_id c11, t1.group_id c12 
    // from o_customer t0 
    // left outer join contact t1 on t1.customer_id = t0.id  
    // where t0.id = ?   order by t0.id; --bind(1)
    
    Assert.assertTrue(custSecondarySql.contains("from o_customer t0 "));
    Assert.assertTrue(custSecondarySql.contains("left outer join contact t1 on t1.customer_id = t0.id "));
    Assert.assertTrue(custSecondarySql.contains("where t0.id "));

    
    SpiQuery<?> orderDetailsSecondaryQuery = secondaryQueries.get(1);
    String ordSecondarySql = orderDetailsSecondaryQuery.getGeneratedSql();

    // select ... 
    // from o_order_detail t0 
    // where (t0.order_id) in (?,?,?,?,?) ; --bind(1,4,1,1,1)

    Assert.assertTrue(ordSecondarySql.contains(" from o_order_detail t0 where (t0.order_id) in (?"));
  }

}
