package com.avaje.tests.text.json;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.BeanState;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.ebean.text.json.JsonWriteOptions;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.Product;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestTextJsonReferenceBean extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    JsonContext jsonContext = Ebean.createJsonContext();

    Product product = Ebean.getReference(Product.class, 1);

    BeanState beanState0 = Ebean.getBeanState(product);
    if (!beanState0.isReference()) {
      // got a cached value from beanCache

    } else {

      String jsonString = jsonContext.toJsonString(product);
      System.out.println(jsonString);

      Product refProd = jsonContext.toBean(Product.class, jsonString);

      BeanState beanState = Ebean.getBeanState(refProd);
      Assert.assertTrue(beanState.isReference());

      String name = refProd.getName();
      Assert.assertNotNull(name);
    }

    List<Order> orders = Ebean.find(Order.class)
    // .setUseCache(false)
        .select("status, orderDate, shipDate, customer").fetch("details", "*")
        // .fetch("details.product","id")
        .order().asc("id").findList();

    Order order = orders.get(0);

    JsonWriteOptions options = new JsonWriteOptions();
    options.setPathProperties("details.product", "id");

    String jsonOrder = jsonContext.toJsonString(order, true, options);
    System.out.println(jsonOrder);

    Order o2 = jsonContext.toBean(Order.class, jsonOrder);

    BeanState beanStateCust = Ebean.getBeanState(o2.getCustomer());
    Assert.assertTrue(beanStateCust.isReference());

  }

}
