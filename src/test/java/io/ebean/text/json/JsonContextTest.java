package io.ebean.text.json;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.text.PathProperties;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import com.fasterxml.jackson.core.JsonGenerator;
import org.junit.Test;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.junit.Assert.*;

public class JsonContextTest {

  @Test
  public void testIsSupportedType() throws Exception {

    EbeanServer server = Ebean.getDefaultServer();

    JsonContext json = server.json();
    assertTrue(json.isSupportedType(Customer.class));
    assertFalse(json.isSupportedType(System.class));
  }

  @Test
  public void test_jsonWithPersistenceContext() {

    ResetBasicData.reset();

    List<Order> orders = Ebean.find(Order.class)
      .fetch("customer", "id, name")
      .where().eq("customer.id", 1)
      .findList();

    String json = Ebean.json().toJson(orders);

    List<Order> orders1 = Ebean.json().toList(Order.class, json);

    Customer customer = null;
    for (Order order : orders1) {
      Customer tempCustomer = order.getCustomer();
      if (customer == null) {
        customer = tempCustomer;
      } else {
        assertThat(tempCustomer).isSameAs(customer);
      }
    }
  }

  @Test
  public void test_json_loadContext() {

    ResetBasicData.reset();

    List<Order> orders = Ebean.find(Order.class)
      .select("status")
      .fetch("customer", "id, name")
      .findList();

    String json = Ebean.json().toJson(orders);

    JsonReadOptions options = new JsonReadOptions().setEnableLazyLoading(true);

    List<Order> orders1 = Ebean.json().toList(Order.class, json, options);

    for (Order order : orders1) {
      Customer customer = order.getCustomer();
      customer.getName();
      customer.getSmallnote();
      List<Contact> contacts = customer.getContacts();
      contacts.size();
    }
  }

  @Test
  public void test_toObject() throws Exception {

    EbeanServer server = Ebean.getDefaultServer();

    JsonContext json = server.json();

    Customer customer = new Customer();
    customer.setId(1);
    customer.setName("Jim");

    String asJson = json.toJson(customer);

    Object bean = json.toObject(Customer.class, asJson);

    assertTrue(bean instanceof Customer);
    assertEquals(Integer.valueOf(1), ((Customer) bean).getId());
    assertEquals("Jim", ((Customer) bean).getName());

    StringReader reader = new StringReader(asJson);
    bean = json.toObject(Customer.class, reader);
    assertTrue(bean instanceof Customer);
    assertEquals(Integer.valueOf(1), ((Customer) bean).getId());
    assertEquals("Jim", ((Customer) bean).getName());
  }

  @Test
  public void test_unknownProperty() {

    String jsonWithUnknown = "{\"id\":42,\"unknownProp\":\"foo\",\"name\":\"rob\",\"version\":1}";

    Customer customer = Ebean.json().toBean(Customer.class, jsonWithUnknown);
    assertEquals(Integer.valueOf(42), customer.getId());
    assertEquals("rob", customer.getName());
  }

  class CustReadVisitor implements JsonReadBeanVisitor<Customer> {

    Customer bean;
    Map<String, Object> unmapped;

    @Override
    public void visit(Customer bean, Map<String, Object> unmapped) {
      this.bean = bean;
      this.unmapped = unmapped;
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void test_unknownProperty_withVisitor() {

    String jsonWithUnknown = "{\"id\":42,\"unknownProp\":\"foo\",\"name\":\"rob\",\"version\":1,\"extraProp\":{\"name\":\"foobie\",\"sim\":\"bo\"}}";

    CustReadVisitor custReadVisitor = new CustReadVisitor();
    JsonReadOptions options = new JsonReadOptions();
    options.addRootVisitor(custReadVisitor);


    Customer customer = Ebean.json().toBean(Customer.class, jsonWithUnknown, options);
    assertEquals(Integer.valueOf(42), customer.getId());
    assertEquals("rob", customer.getName());

    assertSame(customer, custReadVisitor.bean);
    assertEquals("foo", custReadVisitor.unmapped.get("unknownProp"));
    assertEquals(2, custReadVisitor.unmapped.size());
    assertEquals("foobie", ((Map<String, Object>) custReadVisitor.unmapped.get("extraProp")).get("name"));
    assertEquals("bo", ((Map<String, Object>) custReadVisitor.unmapped.get("extraProp")).get("sim"));

  }

  @Test
  public void test_withVisitor_noUnmapped() {

    String someJsonAllKnown = "{\"id\":42,\"name\":\"rob\",\"version\":1}";

    CustReadVisitor custReadVisitor = new CustReadVisitor();
    JsonReadOptions options = new JsonReadOptions();
    options.addRootVisitor(custReadVisitor);

    Customer customer = Ebean.json().toBean(Customer.class, someJsonAllKnown, options);
    assertEquals(Integer.valueOf(42), customer.getId());
    assertEquals("rob", customer.getName());

    assertSame(customer, custReadVisitor.bean);
    assertNull(custReadVisitor.unmapped);
  }

  @Test
  public void testCreateGenerator() throws Exception {

    EbeanServer server = Ebean.getDefaultServer();

    StringWriter writer = new StringWriter();
    JsonContext json = server.json();
    JsonGenerator generator = json.createGenerator(writer);

    Customer customer = new Customer();
    customer.setId(1);
    customer.setName("Jim");


    // we can use the generator before and after our json.toJson() call
    // ... confirming we are not closing the generator
    generator.writeStartArray();
    json.toJson(customer, generator, PathProperties.parse("id,name"));
    generator.writeEndArray();
    generator.close();

    String jsonString = writer.toString();
    assertTrue(jsonString, jsonString.startsWith("["));
    assertTrue(jsonString, jsonString.endsWith("]"));
    assertTrue(jsonString, jsonString.contains("{\"id\":1,\"name\":\"Jim\"}"));
  }

  @Test
  public void testCreateGenerator_writeRaw() throws Exception {

    EbeanServer server = Ebean.getDefaultServer();

    StringWriter writer = new StringWriter();
    JsonContext json = server.json();
    JsonGenerator generator = json.createGenerator(writer);

    // test that we can write anything via writeRaw()
    generator.writeRaw("START");
    generator.writeStartArray();
    generator.writeStartObject();
    generator.writeNumberField("count", 12);
    generator.writeEndObject();
    generator.writeEndArray();
    generator.writeRaw("END");
    generator.close();

    assertEquals("START[{\"count\":12}]END", writer.toString());
  }
}
