package com.avaje.ebeaninternal.server.loadcontext;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.FetchConfig;
import com.avaje.ebean.Query;
import com.avaje.ebeaninternal.server.core.OrmQueryRequest;
import com.avaje.ebeaninternal.server.core.OrmQueryRequestTestHelper;
import com.avaje.tests.model.basic.Order;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DLoadContextTest extends BaseTestCase {

  OrmQueryRequest<Order> queryRequest(Query<Order> query) {
    return OrmQueryRequestTestHelper.queryRequest(query);
  }

  Query<Order> query() {
    return server().find(Order.class);
  }

  @Test
  public void construct_when_defaults_expect_defaultLazyBatchSizeServerDefaults() {

    OrmQueryRequest<Order> queryRequest = queryRequest(query());
    queryRequest.initTransIfRequired();
    queryRequest.endTransIfRequired();

    DLoadContext graphContext = (DLoadContext)queryRequest.getGraphContext();
    DLoadBeanContext customer = graphContext.getBeanContext("customer");


    assertThat(customer.firstBatchSize).isEqualTo(10);
    assertThat(customer.secondaryBatchSize).isEqualTo(10);
  }

  @Test
  public void construct_when_fetchQuery_expect_100_batchSize() {

    OrmQueryRequest<Order> queryRequest = queryRequest(query().fetch("customer",new FetchConfig().query()));
    queryRequest.initTransIfRequired();
    queryRequest.endTransIfRequired();

    DLoadContext graphContext = (DLoadContext)queryRequest.getGraphContext();
    DLoadBeanContext customer = graphContext.getBeanContext("customer");

    assertThat(customer.firstBatchSize).isEqualTo(100);
    assertThat(customer.secondaryBatchSize).isEqualTo(100);
  }

  @Test
  public void construct_when_fetchQuery50_expect_50_batchSize() {

    OrmQueryRequest<Order> queryRequest = queryRequest(query().fetch("customer",new FetchConfig().query(50)));
    queryRequest.initTransIfRequired();
    queryRequest.endTransIfRequired();

    DLoadContext graphContext = (DLoadContext)queryRequest.getGraphContext();
    DLoadBeanContext customer = graphContext.getBeanContext("customer");

    assertThat(customer.firstBatchSize).isEqualTo(50);
    assertThat(customer.secondaryBatchSize).isEqualTo(50);
  }

  @Test
  public void construct_when_fetchQueryFirst20Lazy5_expect_20_5_batchSize() {

    OrmQueryRequest<Order> queryRequest = queryRequest(query().fetch("customer",new FetchConfig().queryFirst(20).lazy(5)));
    queryRequest.initTransIfRequired();
    queryRequest.endTransIfRequired();

    DLoadContext graphContext = (DLoadContext)queryRequest.getGraphContext();
    DLoadBeanContext customer = graphContext.getBeanContext("customer");

    assertThat(customer.firstBatchSize).isEqualTo(20);
    assertThat(customer.secondaryBatchSize).isEqualTo(5);
  }

}