package io.ebean.plugin;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import org.tests.model.basic.Customer;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;


public class SpiServerTest extends BaseTestCase {

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
