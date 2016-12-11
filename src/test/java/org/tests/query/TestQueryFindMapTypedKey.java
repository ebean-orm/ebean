package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Product;
import org.tests.model.basic.ResetBasicData;
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
