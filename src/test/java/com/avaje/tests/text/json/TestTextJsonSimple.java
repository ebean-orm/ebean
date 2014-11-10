package com.avaje.tests.text.json;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.ebean.text.json.JsonWriteOptions;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestTextJsonSimple extends BaseTestCase {

  @Test
  public void test() throws IOException {

    ResetBasicData.reset();

    List<Customer> list = Ebean.find(Customer.class).select("id, name, status, shippingAddress")
        .fetch("billingAddress", "line1, city").fetch("billingAddress.country", "*")
        .fetch("contacts", "firstName,email")
        .order().desc("id").findList();

    EbeanServer server = Ebean.getServer(null);

    JsonContext json = server.createJsonContext();

    JsonWriteOptions options = new JsonWriteOptions();
    options.setRootPathProperties("name, id");
   
    String s = json.toJsonString(list);
    System.out.println(s);

    List<Customer> mList = json.toList(Customer.class, s);
    System.out.println(mList);

  }
}
