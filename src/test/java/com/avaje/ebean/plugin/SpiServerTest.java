package com.avaje.ebean.plugin;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.tests.model.basic.Customer;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;


public class SpiServerTest {

  @Test
  public void test() {

    EbeanServer defaultServer = Ebean.getDefaultServer();
    SpiServer pluginApi = defaultServer.getPluginApi();

    BeanType<Customer> beanType = pluginApi.getBeanType(Customer.class);
    assertEquals("o_customer", beanType.getBaseTable());
    assertNotNull(pluginApi.getDatabasePlatform());
    assertNull(beanType.getFindController());
    assertNotNull(beanType.getPersistController());
    assertNull(beanType.getPersistListener());
    assertNull(beanType.getQueryAdapter());

    assertTrue(beanType.isValidExpression("name"));
    assertTrue(beanType.isValidExpression("contacts.firstName"));
    assertTrue(beanType.isValidExpression("contacts.group.name"));
    assertFalse(beanType.isValidExpression("junk"));
    assertFalse(beanType.isValidExpression("Name"));
    assertFalse(beanType.isValidExpression("contacts.name"));

    Customer customer = new Customer();
    customer.setId(42);

    assertEquals(42, beanType.getBeanId(customer));

    List<? extends BeanType<?>> beanTypes = pluginApi.getBeanTypes("o_customer");
    assertEquals(1, beanTypes.size());
    assertSame(beanType, beanTypes.get(0));

    List<? extends BeanType<?>> allTypes = pluginApi.getBeanTypes();
    assertTrue(!allTypes.isEmpty());
  }

}