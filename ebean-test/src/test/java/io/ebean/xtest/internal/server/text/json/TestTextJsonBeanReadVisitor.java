package io.ebean.xtest.internal.server.text.json;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.text.json.JsonContext;
import io.ebean.text.json.JsonReadBeanVisitor;
import io.ebean.text.json.JsonReadOptions;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Address;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTextJsonBeanReadVisitor extends BaseTestCase {


  @Test
  public void test() {

    ResetBasicData.reset();

    List<Customer> list = DB.find(Customer.class)
      .select("id, name, status, shippingAddress")
      .fetch("billingAddress", "line1, city")
      .fetch("billingAddress.country", "*")
      .fetch("contacts", "firstName,email")
      .orderBy().desc("id")
      .findList();

    JsonContext json = DB.json();

    JsonReadOptions options = new JsonReadOptions();
    options.addRootVisitor(new CVisitor());
    options.addVisitor("contacts", new ContactVisitor());
    options.addVisitor("billingAddress", new AVisitor());
    options.addVisitor("shippingAddress", new ASVisitor());

    String s = json.toJson(list);

    List<Customer> mList = json.toList(Customer.class, s, options);

    StringReader reader = new StringReader(s);
    List<Customer> mList2 = json.toList(Customer.class, reader);

    assertEquals(mList.size(), mList2.size());
  }

  private static class CVisitor implements JsonReadBeanVisitor<Customer> {

    @Override
    public void visit(Customer bean, Map<String, Object> unmapped) {
      bean.getId();
    }
  }

  private static class AVisitor implements JsonReadBeanVisitor<Address> {

    @Override
    public void visit(Address bean, Map<String, Object> unmapped) {
      bean.getId();
    }
  }

  private static class ASVisitor implements JsonReadBeanVisitor<Address> {

    @Override
    public void visit(Address bean, Map<String, Object> unmapped) {
      bean.getId();
    }
  }

  private static class ContactVisitor implements JsonReadBeanVisitor<Contact> {

    @Override
    public void visit(Contact bean, Map<String, Object> unmapped) {
      bean.getId();
    }
  }
}
