package com.avaje.tests.text.json;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.BeanState;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.ebean.text.json.JsonElement;
import com.avaje.ebean.text.json.JsonElementNumber;
import com.avaje.ebean.text.json.JsonReadBeanVisitor;
import com.avaje.ebean.text.json.JsonReadOptions;
import com.avaje.ebean.text.json.JsonWriteBeanVisitor;
import com.avaje.ebean.text.json.JsonWriteOptions;
import com.avaje.ebean.text.json.JsonWriter;
import com.avaje.tests.model.basic.Contact;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestTextJsonBeanReadVisitorWithCustomJson extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    List<Customer> list = Ebean.find(Customer.class).select("id, name, status, shippingAddress")
        .fetch("billingAddress", "line1, city").fetch("billingAddress.country", "*")
        .fetch("contacts", "firstName,email").order().desc("id").findList();

    JsonContext json = Ebean.createJsonContext();

    JsonWriteOptions writeOptions = new JsonWriteOptions();
    writeOptions.setRootPathVisitor(new JsonWriteBeanVisitor<Customer>() {

      public void visit(Customer bean, JsonWriter ctx) {
        System.out.println("write visit customer: " + bean);
        ctx.appendRawValue("dummyCust", "34");
        ctx.appendRawValue("smallCustObject", "{\"a\":34,\"b\":\"asdasdasd\"}");
      }
    });

    writeOptions.setPathProperties("contacts", "firstName,id");
    writeOptions.setPathVisitor("contacts", new JsonWriteBeanVisitor<Contact>() {

      public void visit(Contact bean, JsonWriter ctx) {
        System.out.println("write additional custom json on customer: " + bean);
        ctx.appendRawValue("dummy", "  3400" + bean.getId() + "");
        ctx.appendRawValue("smallObject", "{\"contactA\":34,\"contactB\":\"banana\"}");
      }

    });

    String s = json.toJsonString(list, true, writeOptions);
    System.out.println(s);

    JsonReadOptions readOptions = new JsonReadOptions();
    readOptions.addRootVisitor(new CVisitor());
    readOptions.addVisitor("contacts", new ContactVisitor());

    StringReader reader = new StringReader(s);
    List<Customer> mList2 = json.toList(Customer.class, reader, readOptions);
    System.out.println("VIA READER: " + mList2);

    for (Customer customer : mList2) {
      BeanState beanState = Ebean.getBeanState(customer);
      Assert.assertNotNull(beanState.getLoadedProps());
      Assert.assertTrue(beanState.getLoadedProps().contains("status"));
      Assert.assertTrue(beanState.getLoadedProps().contains("smallnote"));
      Assert.assertFalse(beanState.getLoadedProps().contains("anniversary"));

      String note = customer.getSmallnote();
      Assert.assertEquals("Set in Json Visitor", note);
    }

  }

  private static class CVisitor implements JsonReadBeanVisitor<Customer> {

    public void visit(Customer bean, Map<String, JsonElement> unmapped) {
      System.out.println("visit customer: " + bean);
      bean.setSmallnote("Set in Json Visitor");
    }
  }

  private static class ContactVisitor implements JsonReadBeanVisitor<Contact> {

    public void visit(Contact bean, Map<String, JsonElement> unmapped) {
      System.out.println("visit contact: " + bean);
      Assert.assertNotNull(unmapped);
      JsonElement dummyEl = unmapped.get("dummy");
      JsonElement smallObjEl = unmapped.get("smallObject");
      JsonElementNumber dummyNum = (JsonElementNumber) dummyEl;
      Assert.assertTrue(dummyNum.getValue().startsWith("3400"));
      Assert.assertNotNull(smallObjEl);
    }
  }

}
