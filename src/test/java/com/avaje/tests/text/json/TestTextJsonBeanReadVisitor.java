package com.avaje.tests.text.json;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.ebean.text.json.JsonElement;
import com.avaje.ebean.text.json.JsonReadBeanVisitor;
import com.avaje.ebean.text.json.JsonReadOptions;
import com.avaje.tests.model.basic.Address;
import com.avaje.tests.model.basic.Contact;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestTextJsonBeanReadVisitor extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    List<Customer> list = Ebean.find(Customer.class)
        .select("id, name, status, shippingAddress")
        .fetch("billingAddress", "line1, city")
        .fetch("billingAddress.country", "*")
        .fetch("contacts", "firstName,email")
        .order().desc("id").findList();

    JsonContext json = Ebean.createJsonContext();

    JsonReadOptions options = new JsonReadOptions();
    options.addRootVisitor(new CVisitor());
    options.addVisitor("contacts", new ContactVisitor());
    options.addVisitor("billingAddress", new AVisitor());
    options.addVisitor("shippingAddress", new ASVisitor());

    String s = json.toJsonString(list, true);
    System.out.println(s);

    List<Customer> mList = json.toList(Customer.class, s, options);
    System.out.println("VIA STRING: " + mList);

    StringReader reader = new StringReader(s);
    List<Customer> mList2 = json.toList(Customer.class, reader);
    System.out.println("VIA READER: " + mList2);

    Assert.assertEquals(mList.size(), mList2.size());
  }

  private static class CVisitor implements JsonReadBeanVisitor<Customer> {

    public void visit(Customer bean, Map<String, JsonElement> unmapped) {
      System.out.println("visit customer: " + bean);
    }
  }

  private static class AVisitor implements JsonReadBeanVisitor<Address> {

    public void visit(Address bean, Map<String, JsonElement> unmapped) {
      System.out.println("visit billing address: " + bean);
    }
  }

  private static class ASVisitor implements JsonReadBeanVisitor<Address> {

    public void visit(Address bean, Map<String, JsonElement> unmapped) {
      System.out.println("visit shipping address: " + bean);
    }
  }

  private static class ContactVisitor implements JsonReadBeanVisitor<Contact> {

    public void visit(Contact bean, Map<String, JsonElement> unmapped) {
      System.out.println("visit contact: " + bean);
    }
  }

}
