package com.avaje.tests.iud;

import com.avaje.ebean.BaseTestCase;
import com.avaje.tests.model.info.InfoCompany;
import com.avaje.tests.model.info.InfoCustomer;
import org.junit.Assert;
import org.junit.Test;

public class TestInfoOneToOne extends BaseTestCase {

  @Test
  public void test_cascade_oneToOne() {

    InfoCompany company = new InfoCompany();
    company.setName("info company");

    InfoCustomer customer = new InfoCustomer();
    customer.setName("first info cust");
    customer.setInfos(company);

    customer.save();

    // assert both are inserted
    Assert.assertNotNull(customer.getId());
    Assert.assertNotNull(company.getId());

    // both can be fetched
    Assert.assertNotNull(InfoCustomer.find.byId(customer.getId()));
    Assert.assertNotNull(InfoCompany.find.byId(company.getId()));


    // just update the customer
    customer.setName("first mod");
    customer.update();

    // update the customer and company
    customer.getInfos().setName("2nd mod company");
    customer.setName("2nd mod customer");
    customer.update();

    // fetch and then update both
    InfoCustomer fetchedCustomer = InfoCustomer.find.byId(customer.getId());
    fetchedCustomer.getInfos().setName("3rd mod company");
    fetchedCustomer.setName("3rd mod customer");
    fetchedCustomer.update();


    // delete both customer and company
    fetchedCustomer.delete();
    Assert.assertNull(InfoCustomer.find.byId(customer.getId()));
    Assert.assertNull(InfoCompany.find.byId(company.getId()));

  }

}
