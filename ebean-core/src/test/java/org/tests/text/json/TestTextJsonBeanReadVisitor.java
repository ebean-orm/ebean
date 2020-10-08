package org.tests.text.json;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.text.json.JsonContext;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

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
