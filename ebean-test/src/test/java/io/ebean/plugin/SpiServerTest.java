package io.ebean.plugin;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Database;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.VwCustomer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


public class SpiServerTest extends BaseTestCase {

  @Test
  public void test() {

    Database defaultServer = DB.getDefault();
    SpiServer pluginApi = defaultServer.pluginApi();

    BeanType<Customer> beanType = pluginApi.beanType(Customer.class);
    assertEquals("o_customer", beanType.baseTable());
    assertNotNull(pluginApi.databasePlatform());
    assertNull(beanType.findController());
    assertNotNull(beanType.persistController());
    assertNull(beanType.persistListener());
    assertNull(beanType.queryAdapter());

    assertTrue(beanType.isValidExpression("name"));
    assertTrue(beanType.isValidExpression("contacts.firstName"));
    assertTrue(beanType.isValidExpression("contacts.group.name"));
    assertFalse(beanType.isValidExpression("junk"));
    assertFalse(beanType.isValidExpression("Name"));
    assertFalse(beanType.isValidExpression("contacts.name"));

    Customer customer = new Customer();
    customer.setId(42);

    assertEquals(42, beanType.id(customer));

    List<? extends BeanType<?>> beanTypes = pluginApi.beanTypes("o_customer");
    assertEquals(2, beanTypes.size());

    BeanType<VwCustomer> vwBeanType = pluginApi.beanType(VwCustomer.class);

    assertThat(beanTypes.contains(beanType)).isTrue();
    assertThat(beanTypes.contains(vwBeanType)).isTrue();

    List<? extends BeanType<?>> allTypes = pluginApi.beanTypes();
    assertFalse(allTypes.isEmpty());
  }

}
