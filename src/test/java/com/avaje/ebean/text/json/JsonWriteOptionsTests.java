package com.avaje.ebean.text.json;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.text.PathProperties;
import com.avaje.ebean.text.json.JsonWriteOptions;

import java.util.Set;

public class JsonWriteOptionsTests {

  @Test
  public void test_parse() {
    
    JsonWriteOptions options = JsonWriteOptions.parsePath("id,status,name");
    PathProperties pathProps = options.getPathProperties();
    
    Assert.assertEquals(1, pathProps.getPaths().size());
    Assert.assertTrue(pathProps.get(null).contains("id"));
    Assert.assertTrue(pathProps.get(null).contains("name"));
    Assert.assertTrue(pathProps.get(null).contains("status"));
    Assert.assertFalse(pathProps.get(null).contains("foo"));
  }

  @Test
  public void test_with_depth() {

    JsonWriteOptions options = JsonWriteOptions.parsePath("id,status,name,customer(id,name,address(street,city)),orders(qty,product(sku,prodName))");
    PathProperties pathProps = options.getPathProperties();

    Assert.assertEquals(5, pathProps.getPaths().size());
    Assert.assertTrue(pathProps.get(null).contains("id"));
    Assert.assertTrue(pathProps.get(null).contains("name"));
    Assert.assertTrue(pathProps.get(null).contains("status"));
    Assert.assertTrue(pathProps.get(null).contains("customer"));
    Assert.assertTrue(pathProps.get(null).contains("orders"));
    Assert.assertFalse(pathProps.get(null).contains("foo"));

    Set<String> customer = pathProps.get("customer");
    Assert.assertTrue(customer.contains("id"));
    Assert.assertTrue(customer.contains("name"));
    Assert.assertTrue(customer.contains("address"));

    Set<String> address = pathProps.get("customer.address");
    Assert.assertTrue(address.contains("street"));
    Assert.assertTrue(address.contains("city"));

    Set<String> orders = pathProps.get("orders");
    Assert.assertTrue(orders.contains("qty"));
    Assert.assertTrue(orders.contains("product"));

    Set<String> product = pathProps.get("orders.product");
    Assert.assertTrue(product.contains("sku"));
    Assert.assertTrue(product.contains("prodName"));

  }



}
