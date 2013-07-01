package com.avaje.ebeaninternal.server.query;

import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.FutureIds;
import com.avaje.ebean.FutureList;
import com.avaje.ebean.FutureRowCount;
import com.avaje.ebean.Query;
import com.avaje.ebean.Transaction;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestFutureRowCountErrorHandling  extends BaseTestCase {
  
  @Test
  public void testFutureRowCount() throws InterruptedException {
    
    ResetBasicData.reset();
    
    EbeanServer server = Ebean.getServer(null);
    
    Query<Customer> query = server.createQuery(Customer.class)
      .where().eq("doesNotExist", "this will fail")
      .query();
      
    FutureRowCount<Customer> futureRowCount = server.findFutureRowCount(query, null);
    
    QueryFutureRowCount<Customer> internalRowCount = (QueryFutureRowCount<Customer>)futureRowCount;
    Transaction t = internalRowCount.getTransaction();
    
    try {
      futureRowCount.get();
      Assert.assertTrue("never get here as the SQL is invalid",false);
      
    } catch (ExecutionException e) {
      // Confirm the Transaction has been rolled back 
      Assert.assertFalse("Underlying transaction was rolled back cleanly", t.isActive());
    }
        
  }
  
  @Test
  public void testFutureIds() throws InterruptedException {
    
    ResetBasicData.reset();
    
    EbeanServer server = Ebean.getServer(null);
    
    Query<Customer> query = server.createQuery(Customer.class)
      .where().eq("doesNotExist", "this will fail")
      .query();
      
    FutureIds<Customer> futureIds = server.findFutureIds(query, null);
    
    QueryFutureIds<Customer> internalFuture = (QueryFutureIds<Customer>)futureIds;
    Transaction t = internalFuture.getTransaction();
    
    try {
      internalFuture.get();
      Assert.assertTrue("never get here as the SQL is invalid",false);
      
    } catch (ExecutionException e) {
      // Confirm the Transaction has been rolled back 
      Assert.assertFalse("Underlying transaction was rolled back cleanly", t.isActive());
    }
        
  }

  
  @Test
  public void testFutureList() throws InterruptedException {
    
    ResetBasicData.reset();
    
    EbeanServer server = Ebean.getServer(null);
    
    Query<Customer> query = server.createQuery(Customer.class)
      .where().eq("doesNotExist", "this will fail")
      .query();
      
    FutureList<Customer> futureList = server.findFutureList(query, null);
    
    QueryFutureList<Customer> internalFuture = (QueryFutureList<Customer>)futureList;
    Transaction t = internalFuture.getTransaction();
    
    try {
      internalFuture.get();
      Assert.assertTrue("never get here as the SQL is invalid",false);
      
    } catch (ExecutionException e) {
      // Confirm the Transaction has been rolled back 
      Assert.assertFalse("Underlying transaction was rolled back cleanly", t.isActive());
    }
        
  }
}
