package io.ebeaninternal.server.loadcontext;

import io.ebean.BaseTestCase;
import io.ebean.FetchConfig;
import io.ebean.Query;
import io.ebeaninternal.server.core.OrmQueryRequest;
import io.ebeaninternal.server.core.OrmQueryRequestTestHelper;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import org.tests.model.basic.Order;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DLoadContextTest extends BaseTestCase {

  private OrmQueryRequest<Order> queryRequest(Query<Order> query) {
    return OrmQueryRequestTestHelper.queryRequest(query);
  }

  private Query<Order> query() {
    return server().find(Order.class);
  }

  @Test
  public void construct_when_defaults_expect_defaultLazyBatchSizeServerDefaults() {

    OrmQueryRequest<Order> queryRequest = queryRequest(query());
    queryRequest.initTransIfRequired();
    queryRequest.endTransIfRequired();

    DLoadContext graphContext = (DLoadContext) queryRequest.getGraphContext();
    DLoadBeanContext customer = graphContext.getBeanContext("customer");

    assertThat(customer.batchSize).isEqualTo(10);
  }

  @Test
  public void construct_when_fetchQuery_expect_100_batchSize() {

    OrmQueryRequest<Order> queryRequest = queryRequest(query().fetch("customer", FetchConfig.ofQuery()));
    queryRequest.initTransIfRequired();
    queryRequest.endTransIfRequired();

    DLoadContext graphContext = (DLoadContext) queryRequest.getGraphContext();
    DLoadBeanContext customer = graphContext.getBeanContext("customer");

    assertThat(customer.batchSize).isEqualTo(100);
  }

  @Test
  public void construct_when_fetchQuery_expect_100_batchSize_viaFetchQuery() {

    OrmQueryRequest<Order> queryRequest = queryRequest(query().fetchQuery("customer"));
    queryRequest.initTransIfRequired();
    queryRequest.endTransIfRequired();

    DLoadContext graphContext = (DLoadContext) queryRequest.getGraphContext();
    DLoadBeanContext customer = graphContext.getBeanContext("customer");

    assertThat(customer.batchSize).isEqualTo(100);
  }

  @Test
  public void construct_when_fetchQuery50_expect_50_batchSize() {

    OrmQueryRequest<Order> queryRequest = queryRequest(query().fetch("customer", FetchConfig.ofQuery(50)));
    queryRequest.initTransIfRequired();
    queryRequest.endTransIfRequired();

    DLoadContext graphContext = (DLoadContext) queryRequest.getGraphContext();
    DLoadBeanContext customer = graphContext.getBeanContext("customer");

    assertThat(customer.batchSize).isEqualTo(50);
  }

  @Test
  public void construct_when_fetchQueryFirst20Lazy5_expect_20_5_batchSize() {

    OrmQueryRequest<Order> queryRequest = queryRequest(query().fetchQuery("customer"));
    queryRequest.initTransIfRequired();
    queryRequest.endTransIfRequired();

    DLoadContext graphContext = (DLoadContext) queryRequest.getGraphContext();
    DLoadBeanContext customer = graphContext.getBeanContext("customer");

    assertThat(customer.batchSize).isEqualTo(100);
  }

  @Test
  public void construct_when_fetch_expect_100_100_batchSize() {

    BeanPropertyAssocMany<?> many = (BeanPropertyAssocMany<?>)getBeanDescriptor(Order.class).getBeanProperty("details");
    // the fetch is converted to a query join due to the maxRows
    OrmQueryRequest<Order> queryRequest = queryRequest(query().fetch("details").setMaxRows(50));
    queryRequest.initTransIfRequired();
    queryRequest.endTransIfRequired();

    DLoadContext graphContext = (DLoadContext) queryRequest.getGraphContext();
    DLoadManyContext details = graphContext.getManyContext("details", many);

    assertThat(details.batchSize).isEqualTo(100);
  }

}
