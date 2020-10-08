package org.tests.iud;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.info.InfoCompany;
import org.tests.model.info.InfoCustomer;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class TestInfoOneToOne extends BaseTestCase {

  @Test
  public void test_cascade_oneToOne() {

    InfoCompany company = new InfoCompany();
    company.setName("info company");

    InfoCustomer customer = new InfoCustomer();
    customer.setName("first info cust");
    customer.setCompany(company);

    customer.save();

    // assert JsonIgnore working as expected
    String asJson = Ebean.json().toJson(company);
    assertThat(asJson).doesNotContain("contacts");

    // assert both are inserted
    assertNotNull(customer.getId());
    assertNotNull(company.getId());

    // both can be fetched
    assertNotNull(InfoCustomer.find.byId(customer.getId()));
    assertNotNull(InfoCompany.find.byId(company.getId()));


    // just update the customer
    customer.setName("first mod");
    customer.update();

    // update the customer and company
    customer.getCompany().setName("2nd mod company");
    customer.setName("2nd mod customer");
    customer.update();

    // fetch and then update both
    InfoCustomer fetchedCustomer = InfoCustomer.find.byId(customer.getId());
    fetchedCustomer.getCompany().setName("3rd mod company");
    fetchedCustomer.setName("3rd mod customer");
    fetchedCustomer.update();


    // delete both customer and company
    fetchedCustomer.delete();
    assertNull(InfoCustomer.find.byId(customer.getId()));
    assertNull(InfoCompany.find.byId(company.getId()));

  }

}
