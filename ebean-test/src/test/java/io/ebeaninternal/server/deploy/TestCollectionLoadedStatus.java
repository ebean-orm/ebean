package io.ebeaninternal.server.deploy;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.bean.BeanCollection;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebeaninternal.api.SpiEbeanServer;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestCollectionLoadedStatus extends BaseTestCase {

  @Test
  public void test() {

    SpiEbeanServer server = (SpiEbeanServer) DB.getDefault();
    BeanDescriptor<Customer> custDesc = server.descriptor(Customer.class);

    Customer customer = new Customer();
    EntityBean eb = (EntityBean) customer;
    EntityBeanIntercept ebi = eb._ebean_getIntercept();

    BeanProperty contactsProperty = custDesc.beanProperty("contacts");
    assertFalse(ebi.isLoadedProperty(contactsProperty.propertyIndex()));

    Object contactsViaInternal = contactsProperty.getValue(eb);
    assertNull(contactsViaInternal);
    assertFalse(ebi.isLoadedProperty(contactsProperty.propertyIndex()));

    List<Contact> contacts = customer.getContacts();
    assertNotNull(contacts);
    assertTrue(contacts instanceof BeanCollection);
    assertTrue(ebi.isLoadedProperty(contactsProperty.propertyIndex()));
  }

}
