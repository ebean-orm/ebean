package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.tests.model.basic.Contact;
import com.avaje.tests.model.basic.Customer;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class BeanPropertyAssocManyTest extends BaseTestCase {

  BeanDescriptor<Customer> customerDesc = spiEbeanServer().getBeanDescriptor(Customer.class);

  BeanPropertyAssocMany<Customer> contacts() {
    return (BeanPropertyAssocMany<Customer>)customerDesc.getBeanProperty("contacts");
  }

  @Test
  public void createReferenceIfNull_when_notBeanCollection_expect_null() {

    Customer customer = new Customer();
    customer.setContacts(new ArrayList<Contact>());

    BeanCollection<?> ref = contacts().createReferenceIfNull((EntityBean) customer);
    assertNull(ref);
  }

  @Test
  public void createReferenceIfNull_when_null_expect_ref() {

    Customer customer = new Customer();
    customer.setContacts(null);

    BeanCollection<?> ref = contacts().createReferenceIfNull((EntityBean) customer);
    assertNotNull(ref);
    assertTrue(ref.isReference());
  }

}