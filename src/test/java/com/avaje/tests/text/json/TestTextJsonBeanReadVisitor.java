package com.avaje.tests.text.json;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestTextJsonBeanReadVisitor extends BaseTestCase {

  @Test
  public void test() throws IOException {

    ResetBasicData.reset();

    List<Customer> list = Ebean.find(Customer.class)
        .select("id, name, status, shippingAddress")
        .fetch("billingAddress", "line1, city")
        .fetch("billingAddress.country", "*")
        .fetch("contacts", "firstName,email")
        .order().desc("id").findList();

    JsonContext json = Ebean.json();


    String s = json.toJson(list);

    List<Customer> mList = json.toList(Customer.class, s);

    StringReader reader = new StringReader(s);
    List<Customer> mList2 = json.toList(Customer.class, reader);

    Assert.assertEquals(mList.size(), mList2.size());
  }


}
