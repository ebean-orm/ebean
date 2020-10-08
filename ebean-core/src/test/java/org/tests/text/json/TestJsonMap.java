package org.tests.text.json;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.text.PathProperties;
import io.ebean.text.json.JsonContext;
import io.ebean.text.json.JsonWriteOptions;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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

    Map<String, Customer> map = Ebean.find(Customer.class).findMap();

    JsonContext jsonContext = Ebean.json();
    JsonWriteOptions options = JsonWriteOptions.parsePath("(id,status,name)");

    jsonContext.toJson(map, options);

    options = JsonWriteOptions.parsePath("(id,status,name,shippingAddress(id,line1,city),billingAddress(*),contacts(*))");

    jsonContext.toJson(map, options);

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

    Ebean.json().toJson(customers, pathProperties);

    // Assert.assertTrue(jsonString.indexOf("{\"1\":") > -1);
    // Assert.assertTrue(jsonString.indexOf("{\"id\":1,\"status\":\"NEW\",\"name\":\"Rob\"},")
    // > -1);
  }
}
