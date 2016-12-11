package io.ebeaninternal.server.deploy;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.bean.BeanCollection;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebeaninternal.api.SpiEbeanServer;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestCollectionLoadedStatus extends BaseTestCase {

  @Test
  public void test() {

    SpiEbeanServer server = (SpiEbeanServer) Ebean.getServer(null);
    BeanDescriptor<Customer> custDesc = server.getBeanDescriptor(Customer.class);

    Customer customer = new Customer();
    EntityBean eb = (EntityBean) customer;
    EntityBeanIntercept ebi = eb._ebean_getIntercept();

    BeanProperty contactsProperty = custDesc.getBeanProperty("contacts");
    Assert.assertFalse(ebi.isLoadedProperty(contactsProperty.getPropertyIndex()));

    Object contactsViaInternal = contactsProperty.getValue(eb);
    Assert.assertNull(contactsViaInternal);
    Assert.assertFalse(ebi.isLoadedProperty(contactsProperty.getPropertyIndex()));

    List<Contact> contacts = customer.getContacts();
    Assert.assertNotNull(contacts);
    Assert.assertTrue(contacts instanceof BeanCollection);
    Assert.assertTrue(ebi.isLoadedProperty(contactsProperty.getPropertyIndex()));
  }

}
