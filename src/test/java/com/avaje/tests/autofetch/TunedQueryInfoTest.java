package com.avaje.tests.autofetch;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebean.cache.ServerCacheManager;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.autotune.model.Origin;
import com.avaje.ebeaninternal.server.autotune.service.TunedQueryInfo;
import com.avaje.ebeaninternal.server.querydefn.OrmQueryDetail;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;
import org.avaje.ebeantest.LoggedSqlCollector;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Set;

public class TunedQueryInfoTest extends BaseTestCase {

  EbeanServer server = Ebean.getServer(null);

  private void init() {

    ResetBasicData.reset();

    ServerCacheManager serverCacheManager = Ebean.getServer(null).getServerCacheManager();
    serverCacheManager.clearAll();
  }

  @Test
  public void withSelectEmpty() {

    init();

    OrmQueryDetail tunedDetail = new OrmQueryDetail();
    tunedDetail.select("");

    TunedQueryInfo tunedInfo = createTunedQueryInfo(tunedDetail);

    Query<Order> query = server.find(Order.class).setId(1);

    tunedInfo.tuneQuery((SpiQuery<?>) query);

    Order order = query.findUnique();
    EntityBean eb = (EntityBean) order;
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

    TunedQueryInfo tunedInfo = createTunedQueryInfo(tunedDetail);

    Query<Order> query = server.find(Order.class).setId(1);

    tunedInfo.tuneQuery((SpiQuery<?>) query);

    LoggedSqlCollector.start();

    Order order = query.findUnique();
    EntityBean eb = (EntityBean) order;
    EntityBeanIntercept ebi = eb._ebean_getIntercept();

    Assert.assertFalse(ebi.isFullyLoadedBean());

    // id and any ToMany relationships
    Set<String> loadedPropertyNames = ebi.getLoadedPropertyNames();
    Assert.assertNotNull(loadedPropertyNames);

    // invoke lazy loading
    order.getCustomer();

    List<String> loggedSql = LoggedSqlCollector.stop();
    Assert.assertEquals(2, loggedSql.size());

    Assert.assertTrue(trimSql(loggedSql.get(0), 1).contains("select t0.id, t0.id from o_order t0 where t0.id = ?"));
    Assert.assertTrue(trimSql(loggedSql.get(1), 1).contains("select t0.id, t0.status,"));
  }

  @NotNull
  private TunedQueryInfo createTunedQueryInfo(OrmQueryDetail tunedDetail) {
    Origin origin = new Origin();
    origin.setDetail(tunedDetail.asString());
    return new TunedQueryInfo(origin);
  }

  @Test
  public void withSelectSomeIncludeLazyLoaded() {

    init();

    OrmQueryDetail tunedDetail = new OrmQueryDetail();
    tunedDetail.select("status, customer");

    TunedQueryInfo tunedInfo = createTunedQueryInfo(tunedDetail);

    Query<Order> query = server.find(Order.class).setId(1);

    tunedInfo.tuneQuery((SpiQuery<?>) query);

    LoggedSqlCollector.start();

    Order order = query.findUnique();
    EntityBean eb = (EntityBean) order;
    EntityBeanIntercept ebi = eb._ebean_getIntercept();

    Assert.assertFalse(ebi.isFullyLoadedBean());

    Set<String> loadedPropertyNames = ebi.getLoadedPropertyNames();
    Assert.assertNotNull(loadedPropertyNames);

    Assert.assertTrue(loadedPropertyNames.contains("status"));
    Assert.assertTrue(loadedPropertyNames.contains("customer"));

    // no lazy loading expected here
    order.getCustomer();

    List<String> loggedSql = LoggedSqlCollector.stop();
    Assert.assertEquals(1, loggedSql.size());
  }

  @Test
  public void withSelectSome() {

    init();

    OrmQueryDetail tunedDetail = new OrmQueryDetail();
    tunedDetail.select("status");

    TunedQueryInfo tunedInfo = createTunedQueryInfo(tunedDetail);

    Query<Order> query = server.find(Order.class).setId(1);

    tunedInfo.tuneQuery((SpiQuery<?>) query);

    LoggedSqlCollector.start();

    Order order = query.findUnique();
    EntityBean eb = (EntityBean) order;
    EntityBeanIntercept ebi = eb._ebean_getIntercept();

    Assert.assertFalse(ebi.isFullyLoadedBean());

    Set<String> loadedPropertyNames = ebi.getLoadedPropertyNames();
    Assert.assertNotNull(loadedPropertyNames);

    Assert.assertTrue(loadedPropertyNames.contains("status"));
    Assert.assertFalse(loadedPropertyNames.contains("customer"));

    // no lazy loading expected here
    order.getCustomer();

    List<String> loggedSql = LoggedSqlCollector.stop();
    Assert.assertEquals(2, loggedSql.size());
  }

}
