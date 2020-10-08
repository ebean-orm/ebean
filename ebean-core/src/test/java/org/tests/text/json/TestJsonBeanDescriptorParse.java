package org.tests.text.json;

import com.fasterxml.jackson.core.JsonParser;
import io.ebean.BaseTestCase;
import io.ebean.BeanState;
import io.ebean.Ebean;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.json.SpiJsonReader;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.text.json.ReadJson;
import org.junit.Assert;
import org.junit.Test;
import org.tests.model.basic.Customer;

import java.io.IOException;
import java.io.StringReader;
import java.util.Set;

public class TestJsonBeanDescriptorParse extends BaseTestCase {

  @Test
  public void test() throws IOException {

    SpiEbeanServer server = (SpiEbeanServer) Ebean.getServer(null);

    BeanDescriptor<Customer> descriptor = server.getBeanDescriptor(Customer.class);


    StringReader reader = new StringReader("{\"id\":123,\"name\":\"Hello rob\"}");
    JsonParser parser = server.json().createParser(reader);

    SpiJsonReader readJson = new ReadJson(descriptor, parser, null, null);

    Customer customer = descriptor.jsonRead(readJson, null);

    Assert.assertEquals(Integer.valueOf(123), customer.getId());
    Assert.assertEquals("Hello rob", customer.getName());

    BeanState beanState = Ebean.getBeanState(customer);
    Set<String> loadedProps = beanState.getLoadedProps();

    Assert.assertEquals(2, loadedProps.size());
    Assert.assertTrue(loadedProps.contains("id"));
    Assert.assertTrue(loadedProps.contains("name"));

  }

}
