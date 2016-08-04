package com.avaje.tests.query;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Product;
import com.avaje.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

public class TestQueryFindMapTypedKey extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Map<String, Product> productsBySku = Ebean.find(Product.class)
        .setMapKey("sku")
        .findMap();

    assertThat(productsBySku).isNotEmpty();

    Product desk = productsBySku.get("DSK1");
    assertNotNull(desk);

    Map<String, Customer> map = Ebean.find(Customer.class)
        .select("id, name")
        .setMapKey("name")
        .findMap();

    assertNotNull(map);

  }
}
