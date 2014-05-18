package com.avaje.tests.query;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.tests.model.basic.Contact;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.OrderShipment;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestQueryFetchManyTwoDeep extends BaseTestCase {

  @Test
  public void testFetchOneToManyWithChildOneToMany() {

    ResetBasicData.reset();

    // test that join to order.details is not included in the initial query (included in query join)
    Query<Customer> query = Ebean.find(Customer.class)
        .setAutofetch(false)
        .fetch("orders")
        .fetch("orders.details");

    SpiQuery<?> spiQuery  = (SpiQuery<?>)query;
    spiQuery.setLogSecondaryQuery(true);
    
    List<Customer> list = query.findList();
    Assert.assertTrue("has rows", list.size() > 0);
    Assert.assertTrue(query.getGeneratedSql().contains("from o_customer t0 "));
    Assert.assertTrue(query.getGeneratedSql().contains("left outer join o_order t1 on t1.kcustomer_id = t0.id"));
    Assert.assertTrue(query.getGeneratedSql().contains("left outer join o_customer t2 on t2.id = t1.kcustomer_id"));
    Assert.assertFalse(query.getGeneratedSql().contains("join or_order_ship"));

    //select t0.id c0, t0.status c1, t0.name c2, t0.smallnote c3, t0.anniversary c4, t0.cretime c5, t0.updtime c6, t0.billing_address_id c7, t0.shipping_address_id c8, t1.id c9, t1.status c10, t1.order_date c11, t1.ship_date c12, 
    //       t2.name c13, t1.cretime c14, t1.updtime c15, t1.kcustomer_id c16 
    // from o_customer t0 
    // left outer join o_order t1 on t1.kcustomer_id = t0.id  
    // left outer join o_customer t2 on t2.id = t1.kcustomer_id  
    // where t1.order_date is not null  order by t0.id; --bind()
    
    
    List<SpiQuery<?>> secondaryQueries = spiQuery.getLoggedSecondaryQueries();
    Assert.assertNotNull(secondaryQueries);
    Assert.assertEquals(1, secondaryQueries.size());
    
    SpiQuery<?> secondaryQuery = secondaryQueries.get(0);
    String secondarySql = secondaryQuery.getGeneratedSql();
    Assert.assertTrue(secondarySql.contains("from o_order_detail t0 where (t0.order_id) in"));
    
    // select t0.order_id c0, t0.id c1, t0.order_qty c2, t0.ship_qty c3, t0.unit_price c4, t0.cretime c5, t0.updtime c6, t0.order_id c7, t0.product_id c8 
    // from o_order_detail t0 
    // where (t0.order_id) in (?,?,?,?,?) 
    
  }
  
  @Test
  public void testFetchOptionalManyToOneThenDownToMany() {
    
    ResetBasicData.reset();
    
    // test that join to order.details is not included
    Query<OrderShipment> shipQuery = Ebean.find(OrderShipment.class)
        .setAutofetch(false)
        .fetch("order")
        .fetch("order.details");

    List<OrderShipment> shipList = shipQuery.findList();
    Assert.assertTrue("has rows", shipList.size() > 0);
    
    String generatedSql = shipQuery.getGeneratedSql();

    // select ... 
    // from or_order_ship t0 
    // left outer join o_order t1 on t1.id = t0.order_id  
    // left outer join o_customer t3 on t3.id = t1.kcustomer_id  
    // left outer join o_order_detail t2 on t2.order_id = t1.id  
    // where t2.id > 0 ; --bind()
    
    Assert.assertTrue(generatedSql.contains("from or_order_ship t0"));
    // Relationship from OrderShipment to Order is optional so outer join here
    Assert.assertTrue(generatedSql.contains("left outer join o_order t1 on t1.id = t0.order_id"));
    Assert.assertTrue(generatedSql.contains("left outer join o_customer t3 on t3.id = t1.kcustomer_id"));
    Assert.assertTrue(generatedSql.contains("left outer join o_order_detail t2 on t2.order_id = t1.id"));
    
   
    // If OrderShipment to Order is not optional you get inner joins up to o_order_detail (which is a many)
    
    // select ... 
    // from or_order_ship t0 
    // join o_order t1 on t1.id = t0.order_id  
    // join o_customer t3 on t3.id = t1.kcustomer_id  
    // left outer join o_order_detail t2 on t2.order_id = t1.id
    // where t2.id > 0 ; --bind()
  }


  @Test
  public void testFetchMandatoryManyToOneThenDownToMany() {
    
    ResetBasicData.reset();
    
    // test that join to order.details is not included
    Query<Contact> query = Ebean.find(Contact.class)
        .setAutofetch(false)
        .fetch("customer")
        .fetch("customer.orders");

    List<Contact> shipList = query.findList();
    Assert.assertTrue("has rows", shipList.size() > 0);
    
    String generatedSql = query.getGeneratedSql();

    // select ...
    // from contact t0 
    // join o_customer t1 on t1.id = t0.customer_id  
    // left outer join o_order t2 on t2.kcustomer_id = t1.id  
    // left outer join o_customer t3 on t3.id = t2.kcustomer_id 
    // where t2.order_date is not null ; --bind()
    
    Assert.assertTrue(generatedSql.contains("from contact t0 "));
    // Relationship from Contact to Customer is mandatory so inner join here
    Assert.assertTrue(generatedSql.contains("join o_customer t1 on t1.id = t0.customer_id"));
    // outer join on many relationship 'orders'
    Assert.assertTrue(generatedSql.contains("left outer join o_order t2 on t2.kcustomer_id = t1.id"));
    
  }
  
  @Test
  public void testFetchMandatoryManyToOneWithPredicate() {
    
    ResetBasicData.reset();
    
    // test that join to order.details is not included
    Query<Contact> query = Ebean.find(Contact.class)
        .setAutofetch(false)
        .fetch("customer")
        .where().ilike("customer.name", "Rob%")
        .query();

    List<Contact> list = query.findList();
    Assert.assertTrue("has rows", list.size() > 0);
    
    String generatedSql = query.getGeneratedSql();

    // select ... 
    // from contact t0 
    // join o_customer t1 on t1.id = t0.customer_id 
    // where lower(t1.name) like ? ; --bind(rob%)
    
    Assert.assertTrue(generatedSql.contains("from contact t0 "));
    Assert.assertTrue(generatedSql.contains("join o_customer t1 on t1.id = t0.customer_id"));
    Assert.assertTrue(generatedSql.contains("where lower(t1.name) like ?"));
    
  }

}
