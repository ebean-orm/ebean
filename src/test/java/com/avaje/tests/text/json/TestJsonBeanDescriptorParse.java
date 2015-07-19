package com.avaje.tests.text.json;

import java.io.IOException;
import java.io.StringReader;
import java.util.Set;

import com.avaje.ebeaninternal.server.text.json.ReadJson;
import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.BeanState;
import com.avaje.ebean.Ebean;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.tests.model.basic.Customer;
import com.fasterxml.jackson.core.JsonParser;

public class TestJsonBeanDescriptorParse extends BaseTestCase {

  @Test
  public void test() throws IOException {
    
    SpiEbeanServer server = (SpiEbeanServer)Ebean.getServer(null);
    
    BeanDescriptor<Customer> descriptor = server.getBeanDescriptor(Customer.class);
    
    
    StringReader reader = new StringReader("{\"id\":123,\"name\":\"Hello rob\"}");
    JsonParser parser = server.json().createParser(reader);

    ReadJson readJson = new ReadJson(parser, null, null);
    
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
