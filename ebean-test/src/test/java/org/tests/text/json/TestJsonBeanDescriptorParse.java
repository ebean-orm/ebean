package org.tests.text.json;

import com.fasterxml.jackson.core.JsonParser;
import io.ebean.xtest.BaseTestCase;
import io.ebean.BeanState;
import io.ebean.DB;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.json.SpiJsonReader;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.text.json.ReadJson;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;

import java.io.IOException;
import java.io.StringReader;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestJsonBeanDescriptorParse extends BaseTestCase {

  @Test
  public void test() throws IOException {
    SpiEbeanServer server = (SpiEbeanServer) DB.getDefault();

    BeanDescriptor<Customer> descriptor = server.descriptor(Customer.class);

    SpiJsonReader readJson = createRead(server, descriptor);

    Customer customer = descriptor.jsonRead(readJson, null, null);

    assertEquals(Integer.valueOf(123), customer.getId());
    assertEquals("Hello rob", customer.getName());

    BeanState beanState = DB.beanState(customer);
    Set<String> loadedProps = beanState.loadedProps();

    assertEquals(2, loadedProps.size());
    assertTrue(loadedProps.contains("id"));
    assertTrue(loadedProps.contains("name"));
    
    customer.setName("Hello Roland");
    customer.setId(234);
    readJson = createRead(server, descriptor);
    descriptor.jsonRead(readJson, null, customer);
    assertEquals(Integer.valueOf(123), customer.getId());
    assertEquals("Hello rob", customer.getName());
  }

  private SpiJsonReader createRead(SpiEbeanServer server, BeanDescriptor<Customer> descriptor) {
    StringReader reader = new StringReader("{\"id\":123,\"name\":\"Hello rob\"}");
    JsonParser parser = server.json().createParser(reader);

    SpiJsonReader readJson = new ReadJson(descriptor, parser, null, null);
    return readJson;
  }

}
