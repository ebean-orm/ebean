package com.avaje.tests.cache;

import java.util.List;

import com.avaje.tests.model.basic.Country;
import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Address;
import com.avaje.tests.model.basic.Contact;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestCacheCustomer extends BaseTestCase {

  @Test
  public void testUpdateAddress() {

    ResetBasicData.reset();

    Country nz = Ebean.getReference(Country.class, "NZ");
    Address address = new Address();
    address.setLine1("Some Place");
    address.setCity("Auckland");
    address.setCountry(nz);

    Ebean.save(address);

    address.setLine2("Else");
    Ebean.save(address);

    Ebean.delete(address);
  }

  @Test
  public void test() {

    ResetBasicData.reset();

    List<Customer> list = Ebean.find(Customer.class).setAutoTune(false).setLoadBeanCache(true)
        .findList();

    Assert.assertTrue(list.size() > 1);

    for (Customer customer : list) {
      Address billingAddress = customer.getBillingAddress();
      if (billingAddress != null) {
        billingAddress.getLine1();
      }
      Address shippingAddress = customer.getShippingAddress();
      if (shippingAddress != null) {
        shippingAddress.getLine1();
      }
      List<Contact> contacts = customer.getContacts();
      for (Contact contact : contacts) {
        contact.getFirstName();
      }

    }

  }
}
