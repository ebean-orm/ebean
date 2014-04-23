package com.avaje.ebeaninternal.server.deploy;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.tests.model.basic.Contact;
import com.avaje.tests.model.basic.Customer;

public class TestCollectionLoadedStatus extends BaseTestCase {

  @Test
  public void test() {
    
    SpiEbeanServer server = (SpiEbeanServer)Ebean.getServer(null);
    BeanDescriptor<Customer> custDesc = server.getBeanDescriptor(Customer.class);
    
    Customer customer = new Customer();
    EntityBean eb = (EntityBean)customer;
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
