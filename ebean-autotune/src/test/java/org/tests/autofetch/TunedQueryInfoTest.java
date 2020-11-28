package org.tests.autofetch;

//import io.ebean.BaseTestCase;
//import io.ebean.Ebean;
//import io.ebean.EbeanServer;
//import io.ebean.Query;
//import io.ebean.bean.EntityBean;
//import io.ebean.bean.EntityBeanIntercept;
//import io.ebean.cache.ServerCacheManager;
//import io.ebeaninternal.api.SpiQuery;
//import io.ebeaninternal.server.autotune.model.Origin;
//import io.ebeaninternal.server.autotune.service.TunedQueryInfo;
//import io.ebeaninternal.server.querydefn.OrmQueryDetail;
//import org.ebeantest.LoggedSqlCollector;
//import org.junit.Assert;
//import org.junit.Test;
//import org.tests.model.basic.Order;
//import org.tests.model.basic.ResetBasicData;
//
//import java.util.List;
//import java.util.Set;

public class TunedQueryInfoTest extends BaseTestCase {

//  private void init() {
//
//    ResetBasicData.reset();
//
//    ServerCacheManager serverCacheManager = Ebean.getServer(null).getServerCacheManager();
//    serverCacheManager.clearAll();
//  }
//
//  @Test
//  public void withSelectEmpty() {
//
//    init();
//
//    OrmQueryDetail tunedDetail = new OrmQueryDetail();
//    tunedDetail.select("");
//
//    TunedQueryInfo tunedInfo = createTunedQueryInfo(tunedDetail);
//
//    Query<Order> query = server.find(Order.class).setId(1);
//
//    tunedInfo.tuneQuery((SpiQuery<?>) query);
//
//    Order order = query.findOne();
//    EntityBean eb = (EntityBean) order;
//    EntityBeanIntercept ebi = eb._ebean_getIntercept();
//
//    Assert.assertTrue(ebi.isFullyLoadedBean());
//
//    Set<String> loadedPropertyNames = ebi.getLoadedPropertyNames();
//    Assert.assertNull(loadedPropertyNames);
//
//    // invoke lazy loading
//    order.getCustomer();
//  }
//
//  @Test
//  public void withSelectSomethingThatDoesNotExist() {
//
//    init();
//
//    OrmQueryDetail tunedDetail = new OrmQueryDetail();
//    tunedDetail.select("somethingThatDoesNotExist");
//
//    TunedQueryInfo tunedInfo = createTunedQueryInfo(tunedDetail);
//
//    Query<Order> query = server.find(Order.class).setId(1);
//
//    tunedInfo.tuneQuery((SpiQuery<?>) query);
//
//    LoggedSqlCollector.start();
//
//    Order order = query.findOne();
//    EntityBean eb = (EntityBean) order;
//    EntityBeanIntercept ebi = eb._ebean_getIntercept();
//
//    Assert.assertFalse(ebi.isFullyLoadedBean());
//
//    // id and any ToMany relationships
//    Set<String> loadedPropertyNames = ebi.getLoadedPropertyNames();
//    Assert.assertNotNull(loadedPropertyNames);
//
//    // invoke lazy loading
//    order.getCustomer();
//
//    List<String> loggedSql = LoggedSqlCollector.stop();
//    Assert.assertEquals(2, loggedSql.size());
//
//    Assert.assertTrue(trimSql(loggedSql.get(0), 1).contains("select t0.id, t0.id from o_order t0 where t0.id = ?"));
//    Assert.assertTrue(trimSql(loggedSql.get(1), 1).contains("select t0.id, t0.status,"));
//  }
//
//  private TunedQueryInfo createTunedQueryInfo(OrmQueryDetail tunedDetail) {
//    Origin origin = new Origin();
//    origin.setDetail(tunedDetail.asString());
//    return new TunedQueryInfo(origin);
//  }
//
//  @Test
//  public void withSelectSomeIncludeLazyLoaded() {
//
//    init();
//
//    OrmQueryDetail tunedDetail = new OrmQueryDetail();
//    tunedDetail.select("status, customer");
//
//    TunedQueryInfo tunedInfo = createTunedQueryInfo(tunedDetail);
//
//    Query<Order> query = server.find(Order.class).setId(1);
//
//    tunedInfo.tuneQuery((SpiQuery<?>) query);
//
//    LoggedSqlCollector.start();
//
//    Order order = query.findOne();
//    EntityBean eb = (EntityBean) order;
//    EntityBeanIntercept ebi = eb._ebean_getIntercept();
//
//    Assert.assertFalse(ebi.isFullyLoadedBean());
//
//    Set<String> loadedPropertyNames = ebi.getLoadedPropertyNames();
//    Assert.assertNotNull(loadedPropertyNames);
//
//    Assert.assertTrue(loadedPropertyNames.contains("status"));
//    Assert.assertTrue(loadedPropertyNames.contains("customer"));
//
//    // no lazy loading expected here
//    order.getCustomer();
//
//    List<String> loggedSql = LoggedSqlCollector.stop();
//    Assert.assertEquals(1, loggedSql.size());
//  }
//
//  @Test
//  public void withSelectSome() {
//
//    init();
//
//    OrmQueryDetail tunedDetail = new OrmQueryDetail();
//    tunedDetail.select("status");
//
//    TunedQueryInfo tunedInfo = createTunedQueryInfo(tunedDetail);
//
//    Query<Order> query = server.find(Order.class).setId(1);
//
//    tunedInfo.tuneQuery((SpiQuery<?>) query);
//
//    LoggedSqlCollector.start();
//
//    Order order = query.findOne();
//    EntityBean eb = (EntityBean) order;
//    EntityBeanIntercept ebi = eb._ebean_getIntercept();
//
//    Assert.assertFalse(ebi.isFullyLoadedBean());
//
//    Set<String> loadedPropertyNames = ebi.getLoadedPropertyNames();
//    Assert.assertNotNull(loadedPropertyNames);
//
//    Assert.assertTrue(loadedPropertyNames.contains("status"));
//    Assert.assertFalse(loadedPropertyNames.contains("customer"));
//
//    // no lazy loading expected here
//    order.getCustomer();
//
//    List<String> loggedSql = LoggedSqlCollector.stop();
//    Assert.assertEquals(2, loggedSql.size());
//  }

}
