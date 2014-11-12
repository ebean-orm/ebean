package com.avaje.tests.text.json;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class TestTextJsonSimple extends BaseTestCase {

  @Test
  public void test() throws IOException {

    ResetBasicData.reset();

    List<Customer> list = Ebean.find(Customer.class).select("id, name, status, shippingAddress")
        .fetch("billingAddress", "line1, city").fetch("billingAddress.country", "*")
        .fetch("contacts", "firstName,email")
        .order().desc("id").findList();

    EbeanServer server = Ebean.getServer(null);

    JsonContext json = server.json();

    String jsonOutput = json.toJson(list);
    System.out.println(jsonOutput);

    List<Customer> mList = json.toList(Customer.class, jsonOutput);

    Assert.assertEquals(list.size(), mList.size());
  }
}
