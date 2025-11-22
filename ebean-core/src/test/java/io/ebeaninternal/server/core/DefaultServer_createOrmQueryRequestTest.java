package io.ebeaninternal.server.core;


import io.ebean.DB;
import io.ebean.Database;
import io.ebean.FetchConfig;
import io.ebean.Query;
import io.ebeaninternal.server.querydefn.DefaultOrmQuery;
import io.ebeaninternal.server.querydefn.OrmQueryDetail;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Building;
import org.tests.model.basic.Clan;
import org.tests.model.basic.ClanQuest;
import org.tests.model.basic.Order;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultServer_createOrmQueryRequestTest {

  Query<Order> query() {
    return server().find(Order.class);
  }

  private Database server() {
    return DB.getDefault();
  }

  OrmQueryRequest<Order> queryRequest(Query<Order> query) {
    return OrmQueryRequestTestHelper.queryRequest(query);
  }

  OrmQueryDetail detail(Query<Order> query) {
    return queryRequest(query).query().detail();
  }

  @Test
  public void when_empty_then_same() {

    assertSame(detail(query()), detail(query()));
  }

  @Test
  public void select_all_then_same() {

    assertSame(detail(query().select("*")), detail(query().select("*")));
  }

  @Test
  public void when_propertiesInOrder_then_same() {

    assertSame(detail(query().select("id,name")), detail(query().select("id,name")));
  }

  @Test
  public void when_propertiesInDifferentOrder_then_different() {

    assertDifferent(detail(query().select("id,name")), detail(query().select("name, id")));

    // but same from autotune perspective
    assertThat(detail(query().select("id,name")).isAutoTuneEqual(detail(query().select("name, id")))).isTrue();
  }

  @Test
  public void when_additional_fetch_then_different() {

    assertDifferent(detail(query().select("id,name")), detail(query().select("id,name").fetch("details")));
  }

  @Test
  public void when_same_fetch_then_same() {

    assertSame(detail(query().select("id,name").fetch("details")), detail(query().select("id,name").fetch("details")));
  }

  @Test
  public void when_fetch_order_different_then_different() {

    assertDifferent(detail(query().select("id,name").fetch("details").fetch("customer")),
      detail(query().select("id,name").fetch("customer").fetch("details")));
  }

  @Test
  public void when_extra_queryFetchToMany_then_same() {

    assertDifferent(detail(query().select("id,name").fetch("customer")),
      detail(query().select("id,name").fetch("customer").fetch("details", FetchConfig.ofQuery())));
  }

  @Test
  public void when_extra_queryToOne_fetch_then_different() {

    // with the fetch of customer the foreign key must be added to the root query
    assertDifferent(detail(query().select("id,name")),
      detail(query().select("id,name").fetch("customer", FetchConfig.ofQuery())));
  }

  @Test
  public void when_additional_fetch_V2_then_different() {

    assertDifferent(detail(query().select("id,name")), detail(query().select("id,name").fetch("customer", "id")));
  }


  @Test
  public void when_fetchConfig_then_differentPlan() throws Exception {

    DefaultOrmQuery<Order> query1 = (DefaultOrmQuery<Order>) DB.find(Order.class)
      .select("status, shipDate")
      .fetchQuery("details", "orderQty, unitPrice")
      .fetch("details.product", "sku, name");


    DefaultOrmQuery<Order> query2 = (DefaultOrmQuery<Order>) DB.find(Order.class)
      .select("status, shipDate")
      .fetch("details", "orderQty, unitPrice")
      .fetch("details.product", "sku, name");

    assertDifferent(detail(query1), detail(query2));
  }

  @Test
  public void testJoinOrder_when_fetchJoins_expect_detailJoinsPreserveOrder() {

    Query<Order> query = DB.find(Order.class)
      .select("status, orderDate")
      .fetch("customer", "name")
      .fetch("details");

    OrmQueryRequest<Order> queryRequest = queryRequest(query);
    OrmQueryDetail detail = queryRequest.query().detail();

    assertThat(detail.getFetchPaths()).containsExactly("customer", "details");
  }

  @Test
  public void testJoinOrder_when_fetchJoinsAndWhere_expect_fetchJoinsOnlyInFetchPaths() {

    Query<Order> query = DB.find(Order.class)
      .select("status, orderDate")
      .fetch("details")
      .where().eq("customer.name", "rob").query();

    OrmQueryRequest<Order> queryRequest = queryRequest(query);
    OrmQueryDetail detail = queryRequest.query().detail();

    assertThat(detail.getFetchPaths()).containsExactly("details");
  }

  @Test
  public void testJoinOrder_when_queryFetch_expect_getFetchPaths_doesNotIncludeQueryJoin() {

    Query<Order> query = DB.find(Order.class)
      .select("status, orderDate")
      .fetch("customer", "name")
      .fetchQuery("details");

    OrmQueryRequest<Order> queryRequest = queryRequest(query);
    OrmQueryDetail detail = queryRequest.query().detail();

    assertThat(detail.getFetchPaths()).containsExactly("customer");
  }

  @Test
  public void testJoinOrder_when_queryFetch_expect_getFetchPaths_doesNotIncludeQueryJoin_via_fetchQuery() {

    Query<Order> query = DB.find(Order.class)
      .select("status, orderDate")
      .fetch("customer", "name")
      .fetchQuery("details");

    OrmQueryRequest<Order> queryRequest = queryRequest(query);
    OrmQueryDetail detail = queryRequest.query().detail();

    assertThat(detail.getFetchPaths()).containsExactly("customer");
  }

  @Test
  public void testJoinOrder_when_lazyFetch_expect_getFetchPaths_doesNotIncludeQueryJoin() {

    Query<Order> query = DB.find(Order.class)
      .select("status, orderDate")
      .fetch("customer", "name")
      .fetch("details", FetchConfig.ofLazy());

    OrmQueryRequest<Order> queryRequest = queryRequest(query);
    OrmQueryDetail detail = queryRequest.query().detail();

    assertThat(detail.getFetchPaths()).containsExactly("customer");
  }

  @Test
  public void testJoinOrder_when_lazyFetch_expect_getFetchPaths_doesNotIncludeQueryJoin_via_fetchLazy() {

    Query<Order> query = DB.find(Order.class)
      .select("status, orderDate")
      .fetch("customer", "name")
      .fetchLazy("details");

    OrmQueryRequest<Order> queryRequest = queryRequest(query);
    OrmQueryDetail detail = queryRequest.query().detail();

    assertThat(detail.getFetchPaths()).containsExactly("customer");
  }

  @Test
  public void testJoinOrder_when_lazyFetchAndHasChildren_expect_getFetchPaths_doesNotIncludeJoinOrChild() {

    Query<Order> query = DB.find(Order.class)
      .select("status, orderDate")
      .fetch("customer", "name")
      .fetchLazy("details")
      .fetch("details.product");

    OrmQueryRequest<Order> queryRequest = queryRequest(query);
    OrmQueryDetail detail = queryRequest.query().detail();

    assertThat(detail.getFetchPaths()).containsExactly("customer");
  }

  @Test
  public void testJoinOrder_when_fetchMany_expect_getFetchPaths_containsAllInOrder() {

    Query<Order> query = DB.find(Order.class)
      .select("status, orderDate")
      .fetch("details")
      .fetch("details.product")
      .fetch("customer", "name");

    OrmQueryRequest<Order> queryRequest = queryRequest(query);
    OrmQueryDetail detail = queryRequest.query().detail();

    assertThat(detail.getFetchPaths()).containsExactly("details", "details.product", "customer");
  }

  @Test
  public void testJoinOrder_when_queryJoin_expect_getFetchPaths_excludesQueryJoinAndChildren() {

    Query<Order> query = DB.find(Order.class)
      .select("status, orderDate")
      .fetchQuery("details")
      .fetch("details.product")
      .fetch("customer", "name");

    OrmQueryRequest<Order> queryRequest = queryRequest(query);
    OrmQueryDetail detail = queryRequest.query().detail();

    assertThat(detail.getFetchPaths()).containsExactly("customer");
  }

  @Test
  public void test_removeJoinToMany_when_multipleManyPaths() {

    Query<Order> query = DB.find(Order.class)
      .fetch("details")
      .fetch("details.product")
      .fetch("customer")
      .fetch("customer.contacts"); // second many path

    OrmQueryRequest<Order> queryRequest = queryRequest(query);
    OrmQueryDetail detail = queryRequest.query().detail();

    assertThat(detail.getFetchPaths()).containsExactly("details", "details.product", "customer");
  }

  @Test
  public void test_removeAllJoinToMany_when_firstRow() {

    Query<Order> query = DB.find(Order.class)
      .setFirstRow(1)
      .fetch("details") // many path
      .fetch("details.product")
      .fetch("customer")
      .fetch("customer.contacts"); // many path

    OrmQueryRequest<Order> queryRequest = queryRequest(query);
    OrmQueryDetail detail = queryRequest.query().detail();

    assertThat(detail.getFetchPaths()).containsExactly("customer");
  }

  @Test
  public void test_removeAllJoinToMany_when_maxRows() {

    Query<Order> query = DB.find(Order.class)
      .setMaxRows(1)
      .fetch("details") // many path
      .fetch("details.product")
      .fetch("customer")
      .fetch("customer.contacts"); // many path

    OrmQueryRequest<Order> queryRequest = queryRequest(query);
    OrmQueryDetail detail = queryRequest.query().detail();

    assertThat(detail.getFetchPaths()).containsExactly("customer");
  }

  @Test
  public void test_filterMany_included() {

    Query<Order> query = DB.find(Order.class)
      .fetch("details")
      .fetch("details.product")
      .fetch("customer")
      .fetch("customer.contacts")
      .filterMany("details").eq("orderQuantity", 10)
      .query();

    OrmQueryRequest<Order> queryRequest = queryRequest(query);
    OrmQueryDetail detail = queryRequest.query().detail();

    assertThat(detail.getFetchPaths()).containsExactly("details", "details.product", "customer");
  }

  @Test
  public void test_filterMany_excludedByOrdering() {

    Query<Order> query = DB.find(Order.class)
      .fetch("customer")
      .fetch("customer.contacts")
      .fetch("details")
      .fetch("details.product")
      .filterMany("details").eq("orderQuantity", 10)
      .query();

    OrmQueryRequest<Order> queryRequest = queryRequest(query);
    OrmQueryDetail detail = queryRequest.query().detail();

    assertThat(detail.getFetchPaths()).containsExactly("customer", "customer.contacts");
  }

  @Test
  public void test_filterMany_excludedExplicitly() {

    Query<Order> query = DB.find(Order.class)
      .fetchQuery("details")
      .fetch("details.product")
      .fetch("customer")
      .fetch("customer.contacts")
      .filterMany("details").eq("orderQuantity", 10)
      .query();

    OrmQueryRequest<Order> queryRequest = queryRequest(query);
    OrmQueryDetail detail = queryRequest.query().detail();

    assertThat(detail.getFetchPaths()).containsExactly("customer", "customer.contacts");
  }

  @Test
  public void test_filterMany_excludedExplicitly2() {
    Query<ClanQuest> query = DB.find(ClanQuest.class)
      .setId(1)
      .fetch("clan", "buildings")
      .filterMany("clan.buildings").eq("type", Building.CAFE)
      .query();

    OrmQueryRequest<ClanQuest> queryRequest = OrmQueryRequestTestHelper.queryRequest(query);
    OrmQueryDetail detail = queryRequest.query().detail();

    assertThat(detail.getFetchPaths()).containsExactly("clan", "clan.buildings");
  }

  private void assertSame(OrmQueryDetail detail1, OrmQueryDetail detail2) {
    assertThat(hash(detail1)).isEqualTo(hash(detail2));
  }

  private void assertDifferent(OrmQueryDetail detail1, OrmQueryDetail detail2) {
    assertThat(hash(detail1)).isNotEqualTo(hash(detail2));
  }

  private String hash(OrmQueryDetail detail1) {
    StringBuilder sb = new StringBuilder();
    detail1.queryPlanHash(sb);
    return sb.toString();
  }
}
