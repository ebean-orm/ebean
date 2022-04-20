package io.ebeaninternal.server.autotune.service;

import io.ebean.bean.NodeUsageCollector;
import io.ebean.bean.NodeUsageListener;
import io.ebean.bean.ObjectGraphNode;
import io.ebean.bean.ObjectGraphOrigin;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.querydefn.OrmQueryDetail;
import org.junit.jupiter.api.Test;
import org.tests.autofetch.BaseTestCase;
import org.tests.model.basic.Order;

import static org.assertj.core.api.Assertions.assertThat;

public class ProfileOriginTest extends BaseTestCase {

  static class Noop implements NodeUsageListener {
    @Override
    public void collectNodeUsage(NodeUsageCollector.State state) {
      // do nothing
    }
  }

  private final NodeUsageListener listener = new Noop();

  private final BeanDescriptor<Order> desc = getBeanDescriptor(Order.class);

  @Test
  public void buildDetail() {

    NodeUsageCollector c = node("customer");
    c.addUsed("id");
    c.addUsed("name");

    ProfileOrigin po = new ProfileOrigin(null, false, 1, 1);
    po.collectUsageInfo(c.state());

    OrmQueryDetail detail = po.buildDetail(desc);

    assertThat(detail.asString().trim()).isEqualTo("fetch customer (name)");
  }

  @Test
  public void buildDetail_selectFetch() {

    NodeUsageCollector c = node("customer");
    c.addUsed("id");
    c.addUsed("name");

    ProfileOrigin po = new ProfileOrigin(null, false, 1, 1);
    po.collectUsageInfo(c.state());

    c = node(null);
    c.addUsed("orderDate");
    po.collectUsageInfo(c.state());

    OrmQueryDetail detail = po.buildDetail(desc);

    assertThat(detail.asString()).isEqualTo("select (orderDate) fetch customer (name)");
  }

  @Test
  public void buildDetail_expect_mergeFetchToSelect() {

    NodeUsageCollector c = node("customer");
    c.addUsed("id");

    ProfileOrigin po = new ProfileOrigin(null, false, 1, 1);
    po.collectUsageInfo(c.state());

    c = node(null);
    c.addUsed("orderDate");
    po.collectUsageInfo(c.state());

    OrmQueryDetail detail = po.buildDetail(desc);

    assertThat(detail.asString().trim()).isEqualTo("select (orderDate,customer)");
  }

  @Test
  public void buildDetail_expect_mergeFetchToParentFetch() {

    ProfileOrigin po = new ProfileOrigin(null, false, 1, 1);

    NodeUsageCollector c = node(null);
    c.addUsed("orderDate");
    c.addUsed("customer");
    po.collectUsageInfo(c.state());

    c = node("customer");
    c.addUsed("billingAddress");
    po.collectUsageInfo(c.state());

    c = node("customer.billingAddress");
    c.addUsed("id");
    po.collectUsageInfo(c.state());

    OrmQueryDetail detail = po.buildDetail(desc);

    assertThat(detail.asString()).isEqualTo("select (orderDate) fetch customer (billingAddress)");
  }


  @Test
  public void buildDetail_expect_mergeMulit() {

    ProfileOrigin po = new ProfileOrigin(null, false, 1, 1);

    //fetch details (id,orderQty,shipQty,unitPrice)

    NodeUsageCollector c = node(null);
    c.addUsed("customer");
    po.collectUsageInfo(c.state());

    c = node("customer");
    c.addUsed("id");
    c.addUsed("name");
    c.addUsed("note");
    c.addUsed("billingAddress");
    po.collectUsageInfo(c.state());

    //fetch details.product (id,name)
    c = node("customer.billingAddress");
    c.addUsed("id");
    c.addUsed("line1");
    po.collectUsageInfo(c.state());

    OrmQueryDetail detail = po.buildDetail(desc);
    assertThat(detail.asString()).isEqualTo("fetch customer (name,note) fetch customer.billingAddress (line1)");
  }

  private NodeUsageCollector node(String path) {
    ObjectGraphNode node = new ObjectGraphNode((ObjectGraphOrigin)null, path);
    return new NodeUsageCollector(node, listener);
  }

//  @Test
//  public void testQueries() {
//
//    ResetBasicData.reset();
//
//    //DB.createQuery(Order.class, "select (orderDate) fetch customer (billingAddress)").findList();
//
//    // we prefer this first query other the second one
//    DB.createQuery(Order.class, "select (orderDate,customer)").findList();
//    DB.createQuery(Order.class, "select (orderDate) fetch customer (id)").findList();
//
//  }
}
