package com.avaje.tests.text.json;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.avaje.ebean.Query;
import com.avaje.ebean.text.PathProperties;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.ebean.text.json.JsonWriteOptions;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

import static org.junit.Assert.assertTrue;

public class TestJsonMap extends BaseTestCase {

  @Test
  public void test_basic() throws IOException {

    ResetBasicData.reset();

    List<Customer> customers = Ebean.find(Customer.class).findList();

    JsonContext jsonContext = Ebean.json();

    JsonWriteOptions jsonWriteOptions = JsonWriteOptions.parsePath("(id,status,name)");
    String jsonString = jsonContext.toJson(customers, jsonWriteOptions);
    assertTrue(jsonString.contains("{\"id\":1,\"status\":\"NEW\",\"name\":\"Rob\"}"));

    jsonWriteOptions = JsonWriteOptions.parsePath("status,name");
    jsonString = jsonContext.toJson(customers, jsonWriteOptions);
    assertTrue(jsonString.contains("{\"status\":\"NEW\",\"name\":\"Rob\"}"));

  }

  @Test
  public void test() throws IOException {

    ResetBasicData.reset();

    Map<String, Customer> map = Ebean.find(Customer.class).findMap("id", String.class);

    JsonContext jsonContext = Ebean.json();
    JsonWriteOptions options = JsonWriteOptions.parsePath("(id,status,name)");

    String jsonString = jsonContext.toJson(map, options);
    System.out.println(jsonString);

    options = JsonWriteOptions.parsePath("(id,status,name,shippingAddress(id,line1,city),billingAddress(*),contacts(*))");

    jsonString = jsonContext.toJson(map, options);
    System.out.println(jsonString);

    // Assert.assertTrue(jsonString.indexOf("{\"1\":") > -1);
    // Assert.assertTrue(jsonString.indexOf("{\"id\":1,\"status\":\"NEW\",\"name\":\"Rob\"},")
    // > -1);
  }


  @Test
  public void test_applyPathToQuery() throws IOException {

    ResetBasicData.reset();

    PathProperties pathProperties =
            PathProperties.parse("(id,status,name,shippingAddress(id,line1,city),billingAddress(*),contacts(*))");

    List<Customer> customers = Ebean.find(Customer.class)
        .apply(pathProperties)
        .findList();

    String jsonString = Ebean.json().toJson(customers, pathProperties);
    System.out.println(jsonString);


    // Assert.assertTrue(jsonString.indexOf("{\"1\":") > -1);
    // Assert.assertTrue(jsonString.indexOf("{\"id\":1,\"status\":\"NEW\",\"name\":\"Rob\"},")
    // > -1);
  }
}
