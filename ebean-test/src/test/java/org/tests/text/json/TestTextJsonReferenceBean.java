package org.tests.text.json;

import io.ebean.LazyInitialisationException;
import io.ebean.text.json.JsonReadOptions;
import io.ebean.xtest.BaseTestCase;
import io.ebean.BeanState;
import io.ebean.DB;
import io.ebean.bean.EntityBean;
import io.ebean.text.json.JsonContext;
import io.ebean.text.json.JsonWriteOptions;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.Product;
import org.tests.model.basic.ResetBasicData;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class TestTextJsonReferenceBean extends BaseTestCase {

  @Test
  void fromJson_refBean_shouldNotLazyLoadByDefault() {
    ResetBasicData.reset();

    JsonContext jsonContext = DB.json();
    Product productRefBean = jsonContext.toBean(Product.class, "{\"id\": 1}");

    assertThat(DB.beanState(productRefBean).isReference()).isTrue();
    // does not lazy load by default
    assertThrows(LazyInitialisationException.class, productRefBean::getName);
  }

  @Test
  public void test() throws IOException {

    ResetBasicData.reset();

    SpiEbeanServer server = (SpiEbeanServer) DB.getDefault();

    JsonContext jsonContext = DB.json();

    Product product = DB.reference(Product.class, 1);

    BeanState beanState0 = DB.beanState(product);
    if (!beanState0.isReference()) {
      // got a cached value from beanCache

    } else {

      String jsonString = jsonContext.toJson(product);

      Product refProd = jsonContext.toBean(Product.class, jsonString, new JsonReadOptions().setEnableLazyLoading(true));

      BeanDescriptor<Product> prodDesc = server.descriptor(Product.class);
      EntityBean eb = (EntityBean) refProd;
      prodDesc.isReference(eb._ebean_getIntercept());

      BeanState beanState = DB.beanState(refProd);
      assertTrue(beanState.isReference());

      String name = refProd.getName();
      assertNotNull(name);
    }

    List<Order> orders = DB.find(Order.class)
      // .setUseCache(false)
      .select("status, orderDate, shipDate, customer").fetch("details", "*")
      // .fetch("details.product","id")
      .orderBy().asc("id").findList();

    Order order = orders.get(0);

    JsonWriteOptions options = JsonWriteOptions.parsePath("*,details(id,orderQty,product(id))");

    String jsonOrder = jsonContext.toJson(order, options);

    Order o2 = jsonContext.toBean(Order.class, jsonOrder);
    Customer customer = o2.getCustomer();

    BeanDescriptor<Customer> custDesc = server.descriptor(Customer.class);

    assertTrue(custDesc.isReference(((EntityBean) customer)._ebean_getIntercept()));
  }

}
