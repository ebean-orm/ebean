package org.tests.cache;

import io.ebean.BaseTestCase;
import io.ebean.CacheMode;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestCacheCustomer extends BaseTestCase {

  @Test
  public void testUpdateAddress() {

    ResetBasicData.reset();

    Country nz = DB.reference(Country.class, "NZ");
    Address address = new Address();
    address.setLine1("Some Place");
    address.setCity("Auckland");
    address.setCountry(nz);

    DB.save(address);

    address.setLine2("Else");
    DB.save(address);

    DB.delete(address);
  }

  @Test
  public void test() {

    ResetBasicData.reset();

    List<Customer> list = DB.find(Customer.class).setAutoTune(false).setBeanCacheMode(CacheMode.PUT)
      .findList();

    assertTrue(list.size() > 1);

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
