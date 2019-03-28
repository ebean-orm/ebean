package io.ebeaninternal.server.autotune.service;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.bean.NodeUsageCollector;
import io.ebean.bean.ObjectGraphNode;
import io.ebean.bean.ObjectGraphOrigin;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.querydefn.OrmQueryDetail;
import org.junit.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import static org.assertj.core.api.Assertions.assertThat;

public class ProfileOriginTest extends BaseTestCase {


  private BeanDescriptor<Order> desc = getBeanDescriptor(Order.class);

  @Test
  public void buildDetail() {

    NodeUsageCollector c = node("customer");
    c.addUsed("id");
    c.addUsed("name");

    ProfileOrigin po = new ProfileOrigin(null, false, 1, 1);
    po.collectUsageInfo(c);

    OrmQueryDetail detail = po.buildDetail(desc);

    assertThat(detail.asString().trim()).isEqualTo("fetch customer (name)");
  }

  @Test
  public void buildDetail_selectFetch() {

    NodeUsageCollector c = node("customer");
    c.addUsed("id");
    c.addUsed("name");

    ProfileOrigin po = new ProfileOrigin(null, false, 1, 1);
    po.collectUsageInfo(c);

    c = node(null);
    c.addUsed("orderDate");
    po.collectUsageInfo(c);

    OrmQueryDetail detail = po.buildDetail(desc);

    assertThat(detail.asString()).isEqualTo("select (orderDate) fetch customer (name)");
  }

  @Test
  public void buildDetail_expect_mergeFetchToSelect() {

    NodeUsageCollector c = node("customer");
    c.addUsed("id");

    ProfileOrigin po = new ProfileOrigin(null, false, 1, 1);
    po.collectUsageInfo(c);

    c = node(null);
    c.addUsed("orderDate");
    po.collectUsageInfo(c);

    OrmQueryDetail detail = po.buildDetail(desc);

    assertThat(detail.asString().trim()).isEqualTo("select (orderDate,customer)");
  }

  @Test
  public void buildDetail_expect_mergeFetchToParentFetch() {

    ProfileOrigin po = new ProfileOrigin(null, false, 1, 1);

    NodeUsageCollector c = node(null);
    c.addUsed("orderDate");
    c.addUsed("customer");
    po.collectUsageInfo(c);

    c = node("customer");
    c.addUsed("billingAddress");
    po.collectUsageInfo(c);

    c = node("customer.billingAddress");
    c.addUsed("id");
    po.collectUsageInfo(c);

    OrmQueryDetail detail = po.buildDetail(desc);

    assertThat(detail.asString()).isEqualTo("select (orderDate) fetch customer (billingAddress)");
  }


  @Test
  public void buildDetail_expect_mergeMulit() {

    ProfileOrigin po = new ProfileOrigin(null, false, 1, 1);

    //fetch details (id,orderQty,shipQty,unitPrice)

    NodeUsageCollector c = node(null);
    c.addUsed("details");
    po.collectUsageInfo(c);

    c = node("details");
    c.addUsed("id");
    c.addUsed("orderQty");
    c.addUsed("shipQty");
    c.addUsed("unitPrice");
    c.addUsed("product");
    po.collectUsageInfo(c);

    //fetch details.product (id,name)
    c = node("details.product");
    c.addUsed("id");
    c.addUsed("name");
    po.collectUsageInfo(c);

    OrmQueryDetail detail = po.buildDetail(desc);
    assertThat(detail.asString()).isEqualTo("fetch details (orderQty,shipQty,unitPrice) fetch details.product (name)");
  }

  private NodeUsageCollector node(String path) {
    ObjectGraphNode node = new ObjectGraphNode((ObjectGraphOrigin)null, path);
    return new NodeUsageCollector(node, null);
  }

  @Test
  public void testQueries() {

    ResetBasicData.reset();

    //DB.createQuery(Order.class, "select (orderDate) fetch customer (billingAddress)").findList();

    // we prefer this first query other the second one
    DB.createQuery(Order.class, "select (orderDate,customer)").findList();
    DB.createQuery(Order.class, "select (orderDate) fetch customer (id)").findList();

  }
}
