package org.tests.text.json;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.text.json.JsonContext;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTextJsonBeanReadVisitor extends BaseTestCase {

  @Test
  public void test() throws IOException {

    ResetBasicData.reset();

    List<Customer> list = DB.find(Customer.class)
      .select("id, name, status, shippingAddress")
      .fetch("billingAddress", "line1, city")
      .fetch("billingAddress.country", "*")
      .fetch("contacts", "firstName,email")
      .orderBy().desc("id").findList();

    JsonContext json = DB.json();


    String s = json.toJson(list);

    List<Customer> mList = json.toList(Customer.class, s);

    StringReader reader = new StringReader(s);
    List<Customer> mList2 = json.toList(Customer.class, reader);

    assertEquals(mList.size(), mList2.size());
  }


}
