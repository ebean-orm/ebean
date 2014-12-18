package com.avaje.ebean.text.json;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.text.PathProperties;
import com.avaje.tests.model.basic.Customer;
import com.fasterxml.jackson.core.JsonGenerator;
import org.junit.Test;

import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.Assert.*;

public class JsonContextTest {

  @Test
  public void testIsSupportedType() throws Exception {

    EbeanServer server = Ebean.getServer(null);

    JsonContext json = server.json();
    assertTrue(json.isSupportedType(Customer.class));
    assertFalse(json.isSupportedType(System.class));
  }

  @Test
  public void test_toObject() throws Exception {

    EbeanServer server = Ebean.getServer(null);

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

  @Test
  public void testCreateGenerator() throws Exception {

    EbeanServer server = Ebean.getServer(null);

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

    EbeanServer server = Ebean.getServer(null);

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