package io.ebean.text.json;

import io.ebean.FetchPath;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JsonWriteOptionsTests {

  @Test
  public void test_parse() {

    JsonWriteOptions options = JsonWriteOptions.parsePath("id,status,name");
    FetchPath pathProps = options.getPathProperties();

    //Assert.assertEquals(1, pathProps.getPaths().size());
    assertTrue(pathProps.getProperties(null).contains("id"));
    assertTrue(pathProps.getProperties(null).contains("name"));
    assertTrue(pathProps.getProperties(null).contains("status"));
    assertFalse(pathProps.getProperties(null).contains("foo"));
  }

  @Test
  public void test_with_depth() {

    JsonWriteOptions options = JsonWriteOptions.parsePath("id,status,name,customer(id,name,address(street,city)),orders(qty,product(sku,prodName))");
    FetchPath pathProps = options.getPathProperties();

    //Assert.assertEquals(5, pathProps.getPaths().size());
    assertTrue(pathProps.getProperties(null).contains("id"));
    assertTrue(pathProps.getProperties(null).contains("name"));
    assertTrue(pathProps.getProperties(null).contains("status"));
    assertTrue(pathProps.getProperties(null).contains("customer"));
    assertTrue(pathProps.getProperties(null).contains("orders"));
    assertFalse(pathProps.getProperties(null).contains("foo"));

    Set<String> customer = pathProps.getProperties("customer");
    assertTrue(customer.contains("id"));
    assertTrue(customer.contains("name"));
    assertTrue(customer.contains("address"));

    Set<String> address = pathProps.getProperties("customer.address");
    assertTrue(address.contains("street"));
    assertTrue(address.contains("city"));

    Set<String> orders = pathProps.getProperties("orders");
    assertTrue(orders.contains("qty"));
    assertTrue(orders.contains("product"));

    Set<String> product = pathProps.getProperties("orders.product");
    assertTrue(product.contains("sku"));
    assertTrue(product.contains("prodName"));

  }


}
