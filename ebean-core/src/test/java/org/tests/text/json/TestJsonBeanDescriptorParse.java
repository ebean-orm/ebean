package org.tests.text.json;

import com.fasterxml.jackson.core.JsonParser;
import io.ebean.BaseTestCase;
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

    BeanDescriptor<Customer> descriptor = server.getBeanDescriptor(Customer.class);

    StringReader reader = new StringReader("{\"id\":123,\"name\":\"Hello rob\"}");
    JsonParser parser = server.json().createParser(reader);

    SpiJsonReader readJson = new ReadJson(descriptor, parser, null, null);

    Customer customer = descriptor.jsonRead(readJson, null);

    assertEquals(Integer.valueOf(123), customer.getId());
    assertEquals("Hello rob", customer.getName());

    BeanState beanState = DB.getBeanState(customer);
    Set<String> loadedProps = beanState.getLoadedProps();

    assertEquals(2, loadedProps.size());
    assertTrue(loadedProps.contains("id"));
    assertTrue(loadedProps.contains("name"));
  }

}
