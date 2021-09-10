package io.ebeaninternal.server.deploy;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebeaninternal.api.SpiEbeanServer;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.composite.RCustomer;
import org.tests.model.composite.RCustomerKey;
import org.tests.model.embedded.UserInterestLive;
import org.tests.model.embedded.UserInterestLiveKey;

import java.sql.Timestamp;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TestBeanDescriptorHasIdProperty extends BaseTestCase {

  SpiEbeanServer spiServer;

  public TestBeanDescriptorHasIdProperty() {
    Database server = DB.getDefault();
    spiServer = (SpiEbeanServer) server;
  }

  @Test
  public void testHasId() {

    BeanDescriptor<Order> beanDescriptor = spiServer.descriptor(Order.class);
    assertNotNull(beanDescriptor.idProperty());
    assertEquals("id", beanDescriptor.idProperty().name());

    assertNotNull(beanDescriptor.versionProperty());
    assertEquals("updtime", beanDescriptor.versionProperty().name());

    Order order = new Order();

    assertFalse(beanDescriptor.hasIdValue(entityBean(order)));
    assertFalse(beanDescriptor.hasVersionProperty(getIntercept(order)));

    order.setId(23);
    order.setUpdtime(new Timestamp(System.currentTimeMillis()));

    assertTrue(beanDescriptor.hasIdValue(entityBean(order)));
    assertTrue(beanDescriptor.hasVersionProperty(getIntercept(order)));

  }

  @Test
  public void testIsReference() {

    BeanDescriptor<Customer> beanDescriptor = spiServer.descriptor(Customer.class);

    Customer order = new Customer();
    EntityBeanIntercept ebi = getIntercept(order);
    assertFalse(beanDescriptor.referenceIdPropertyOnly(ebi));

    order.setId(23);
    assertTrue(beanDescriptor.referenceIdPropertyOnly(ebi));

    order.setName("custName");
    assertFalse(beanDescriptor.referenceIdPropertyOnly(ebi));
  }

  @Test
  public void isReference_withGeneratedOnInsertOnlyProperty_expect_false() {
    BeanDescriptor<UserInterestLive> descriptor = spiServer.descriptor(UserInterestLive.class);
    UserInterestLive bean = new UserInterestLive(new UserInterestLiveKey(1L, 2L));
    EntityBeanIntercept ebi = getIntercept(bean);
    assertFalse(descriptor.referenceIdPropertyOnly(ebi));
  }

  @Test
  public void test_getIdForJson() {

    BeanDescriptor<Order> orderDesc = spiServer.descriptor(Order.class);

    Order order = new Order();
    order.setId(42);
    assertEquals(42, orderDesc.idForJson(order));

    assertEquals(42, orderDesc.convertIdFromJson(42));
    assertEquals(42, orderDesc.convertIdFromJson("42"));
    assertEquals(42, orderDesc.convertIdFromJson(42L));


    RCustomerKey key = new RCustomerKey();
    key.setCompany("comp");
    key.setName("fred");

    RCustomer rCustomer = new RCustomer();
    rCustomer.setKey(key);

    BeanDescriptor<RCustomer> rcustDesc = spiServer.descriptor(RCustomer.class);

    @SuppressWarnings("unchecked")
    Map<String, Object> idForJson = (Map<String, Object>) rcustDesc.idForJson(rCustomer);
    assertEquals("comp", idForJson.get("company"));
    assertEquals("fred", idForJson.get("name"));
    assertEquals(2, idForJson.size());

    RCustomerKey keyVal = (RCustomerKey) rcustDesc.convertIdFromJson(idForJson);
    assertEquals("comp", keyVal.getCompany());
    assertEquals("fred", keyVal.getName());

  }

  private EntityBean entityBean(Object bean) {
    return (EntityBean) bean;
  }

  private EntityBeanIntercept getIntercept(Object bean) {
    return ((EntityBean) bean)._ebean_getIntercept();
  }

}
