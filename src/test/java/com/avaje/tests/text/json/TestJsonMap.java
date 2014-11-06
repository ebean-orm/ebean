package com.avaje.tests.text.json;

import java.io.IOException;
import java.util.Map;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.ebean.text.json.JsonWriteOptions;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestJsonMap extends BaseTestCase {

  @Test
  public void test() throws IOException {

    ResetBasicData.reset();

    Map<String, Customer> map = Ebean.find(Customer.class).findMap("id", String.class);

    JsonContext jsonContext = Ebean.createJsonContext();
    JsonWriteOptions jsonWriteOptions = JsonWriteOptions.parsePath("(id,status,name)");

    String jsonString = jsonContext.toJsonString(map, jsonWriteOptions);
    System.out.println(jsonString);

    jsonContext = Ebean.createJsonContext();
    jsonWriteOptions = JsonWriteOptions
        .parsePath("(id,status,name,shippingAddress(id,line1,city),billingAddress(*),contacts(*))");
    // jsonWriteOptions =
    // JsonWriteOptions.parsePath("(id,status,billingAddress(*))");

    jsonString = jsonContext.toJsonString(map, jsonWriteOptions);
    System.out.println(jsonString);

    // Assert.assertTrue(jsonString.indexOf("{\"1\":") > -1);
    // Assert.assertTrue(jsonString.indexOf("{\"id\":1,\"status\":\"NEW\",\"name\":\"Rob\"},")
    // > -1);
  }

}
