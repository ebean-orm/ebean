package org.tests.text.json;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.text.PathProperties;
import io.ebean.text.json.JsonContext;
import io.ebean.text.json.JsonReadBeanVisitor;
import io.ebean.text.json.JsonReadOptions;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Address;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TestTextJsonSimple extends BaseTestCase {

  class CustJsonRead implements JsonReadBeanVisitor<Customer> {

    Customer bean;
    Map<String, Object> unmapped;

    @Override
    public void visit(Customer bean, Map<String, Object> unmapped) {
      this.bean = bean;
      this.unmapped = unmapped;
    }
  }

  class ContactJsonRead implements JsonReadBeanVisitor<Contact> {

    Contact bean;
    Map<String, Object> unmapped;

    @Override
    public void visit(Contact bean, Map<String, Object> unmapped) {
      this.bean = bean;
      this.unmapped = unmapped;
    }
  }

  class AddressJsonRead implements JsonReadBeanVisitor<Address> {

    Address bean;
    Map<String, Object> unmapped;

    @Override
    public void visit(Address bean, Map<String, Object> unmapped) {
      this.bean = bean;
      this.unmapped = unmapped;
    }
  }

  @Test
  public void test() throws IOException {

    ResetBasicData.reset();

    List<Customer> list = DB.find(Customer.class)
      .select("id, name, status, shippingAddress")
      .fetch("billingAddress", "line1, city").fetch("billingAddress.country", "*")
      .fetch("contacts", "firstName,email")
      .orderBy().desc("id").findList();

    Database server = DB.getDefault();

    JsonContext json = server.json();

    String jsonOutput = json.toJson(list);

    // check that transient fields are included by default in the JSON output
    assertTrue(jsonOutput.contains("\"selected\":"));

    List<Customer> mList = json.toList(Customer.class, jsonOutput);
    assertEquals(list.size(), mList.size());

    CustJsonRead custJsonRead = new CustJsonRead();
    ContactJsonRead contactJsonRead = new ContactJsonRead();
    AddressJsonRead addressJsonRead = new AddressJsonRead();

    JsonReadOptions options = new JsonReadOptions();
    options.addRootVisitor(custJsonRead);
    options.addVisitor("contacts", contactJsonRead);
    options.addVisitor("billingAddress", addressJsonRead);

    List<Customer> customers = json.toList(Customer.class, jsonOutput, options);

    assertEquals(list.size(), customers.size());

  }

  @Test
  public void testTransientIncludedByDefault() {

    ResetBasicData.reset();

    List<Customer> list = DB.find(Customer.class)
      .select("id, name")
      .orderBy().desc("id").findList();

    Database server = DB.getDefault();

    JsonContext json = server.json();

    // not using pathProperties - includes transient fields by default
    String jsonOutput = json.toJson(list);

    // check that transient fields are included by default in the JSON output
    assertTrue(jsonOutput.contains("\"selected\":"));

    List<Customer> mList = json.toList(Customer.class, jsonOutput);
    assertEquals(list.size(), mList.size());
  }

  @Test
  public void testTransientIncludedExplicitly() {

    ResetBasicData.reset();

    List<Customer> list = DB.find(Customer.class)
      .select("id, name")
      .orderBy().desc("id").findList();

    Database server = DB.getDefault();

    JsonContext json = server.json();

    PathProperties pathProperties = PathProperties.parse("(id,name,selected)");
    String jsonOutput = json.toJson(list, pathProperties);

    // check that transient fields are included by explicit pathProperties
    assertTrue(jsonOutput.contains("\"selected\":"));

    pathProperties = PathProperties.parse("(id,name)");
    jsonOutput = json.toJson(list, pathProperties);

    // check that transient fields are NOT included when explicitly excluded by pathProperties
    assertFalse(jsonOutput.contains("\"selected\":"));
  }

}
