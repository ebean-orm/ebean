package com.avaje.ebeaninternal.server.autofetch;

import java.util.List;
import java.util.Set;

import org.avaje.ebeantest.LoggedSqlCollector;
import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebean.cache.ServerCacheManager;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.autofetch.TunedQueryInfo;
import com.avaje.ebeaninternal.server.querydefn.OrmQueryDetail;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;

public class TunedQueryInfoTest extends BaseTestCase {

  EbeanServer server = Ebean.getServer(null);
  
  private void init() {
    
    ResetBasicData.reset();
    
    ServerCacheManager serverCacheManager = Ebean.getServer(null).getServerCacheManager();
    serverCacheManager.clearAll();
    serverCacheManager.setCaching(Order.class, false);    
  }
  
  @Test
  public void withSelectNull() {
    
    init();
    
    OrmQueryDetail tunedDetail = new OrmQueryDetail();
    tunedDetail.select(null);
    
    TunedQueryInfo tunedInfo = new TunedQueryInfo(null, tunedDetail, 0);
      
    Query<Order> query = server.find(Order.class).setId(1);
    
    tunedInfo.autoFetchTune((SpiQuery<?>)query);
    
    Order order = query.findUnique();
    EntityBean eb = (EntityBean)order;
    EntityBeanIntercept ebi = eb._ebean_getIntercept();
    
    Assert.assertTrue(ebi.isFullyLoadedBean());
    
    Set<String> loadedPropertyNames = ebi.getLoadedPropertyNames();
    Assert.assertNull(loadedPropertyNames);
    
    // invoke lazy loading
    order.getCustomer();
  }
  

  @Test
  public void withSelectEmpty() {
    
    init();
    
    OrmQueryDetail tunedDetail = new OrmQueryDetail();
    tunedDetail.select("");
    
    TunedQueryInfo tunedInfo = new TunedQueryInfo(null, tunedDetail, 0);
      
    Query<Order> query = server.find(Order.class).setId(1);
    
    tunedInfo.autoFetchTune((SpiQuery<?>)query);
    
    Order order = query.findUnique();
    EntityBean eb = (EntityBean)order;
    EntityBeanIntercept ebi = eb._ebean_getIntercept();
    
    Assert.assertTrue(ebi.isFullyLoadedBean());
    
    Set<String> loadedPropertyNames = ebi.getLoadedPropertyNames();
    Assert.assertNull(loadedPropertyNames);
    
    // invoke lazy loading
    order.getCustomer();
  }
  
  @Test
  public void withSelectSomethingThatDoesNotExist() {
    
    init();
    
    OrmQueryDetail tunedDetail = new OrmQueryDetail();
    tunedDetail.select("somethingThatDoesNotExist");
    
    TunedQueryInfo tunedInfo = new TunedQueryInfo(null, tunedDetail, 0);
      
    Query<Order> query = server.find(Order.class).setId(1);
    
    tunedInfo.autoFetchTune((SpiQuery<?>)query);
    
    LoggedSqlCollector.start();
    
    Order order = query.findUnique();
    EntityBean eb = (EntityBean)order;
    EntityBeanIntercept ebi = eb._ebean_getIntercept();
    
    Assert.assertFalse(ebi.isFullyLoadedBean());
    
    // id and any ToMany relationships
    Set<String> loadedPropertyNames = ebi.getLoadedPropertyNames();
    Assert.assertNotNull(loadedPropertyNames);
    
    // invoke lazy loading
    order.getCustomer();
    
    List<String> loggedSql = LoggedSqlCollector.stop();
    Assert.assertEquals(2, loggedSql.size());
    
    Assert.assertTrue(loggedSql.get(0).contains("select t0.id c0, t0.id c1 from o_order t0 where t0.id = ?"));
    Assert.assertTrue(loggedSql.get(1).contains("select t0.id c0, t0.status c1,"));
  }
  
  @Test
  public void withSelectSomeIncludeLazyLoaded() {
    
    init();

    OrmQueryDetail tunedDetail = new OrmQueryDetail();
    tunedDetail.select("status, customer");
    
    TunedQueryInfo tunedInfo = new TunedQueryInfo(null, tunedDetail, 0);
      
    Query<Order> query = server.find(Order.class).setId(1);
    
    tunedInfo.autoFetchTune((SpiQuery<?>)query);
    
    LoggedSqlCollector.start();
    
    Order order = query.findUnique();
    EntityBean eb = (EntityBean)order;
    EntityBeanIntercept ebi = eb._ebean_getIntercept();
    
    Assert.assertFalse(ebi.isFullyLoadedBean());
    
    Set<String> loadedPropertyNames = ebi.getLoadedPropertyNames();
    Assert.assertNotNull(loadedPropertyNames);
    
    Assert.assertTrue(loadedPropertyNames.contains("status"));
    Assert.assertTrue(loadedPropertyNames.contains("customer"));
    
    // no lazy loading expected here
    order.getCustomer();
    
    List<String> loggedSql = LoggedSqlCollector.stop();
    Assert.assertEquals(1,  loggedSql.size());
  }
  
  @Test
  public void withSelectSome() {
    
    init();
    
    OrmQueryDetail tunedDetail = new OrmQueryDetail();
    tunedDetail.select("status");
    
    TunedQueryInfo tunedInfo = new TunedQueryInfo(null, tunedDetail, 0);
      
    Query<Order> query = server.find(Order.class).setId(1);
    
    tunedInfo.autoFetchTune((SpiQuery<?>)query);
    
    LoggedSqlCollector.start();
    
    Order order = query.findUnique();
    EntityBean eb = (EntityBean)order;
    EntityBeanIntercept ebi = eb._ebean_getIntercept();
    
    Assert.assertFalse(ebi.isFullyLoadedBean());
    
    Set<String> loadedPropertyNames = ebi.getLoadedPropertyNames();
    Assert.assertNotNull(loadedPropertyNames);
    
    Assert.assertTrue(loadedPropertyNames.contains("status"));
    Assert.assertFalse(loadedPropertyNames.contains("customer"));
    
    // no lazy loading expected here
    order.getCustomer();
    
    List<String> loggedSql = LoggedSqlCollector.stop();
    Assert.assertEquals(2,  loggedSql.size());
  }
 
}
