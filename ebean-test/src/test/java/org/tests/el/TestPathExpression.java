package org.tests.el;

import io.ebean.DB;
import io.ebean.bean.EntityBean;
import io.ebean.plugin.BeanType;
import io.ebean.plugin.ExpressionPath;
import io.ebean.plugin.SpiServer;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Address;
import org.tests.model.basic.Country;
import org.tests.model.basic.Customer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestPathExpression {

  final BeanType<Customer> beanType;
  final ExpressionPath billingId;
  final ExpressionPath line1;
  final ExpressionPath city;

  public TestPathExpression() {
    SpiServer server = DB.getDefault().pluginApi();
    beanType = server.beanType(Customer.class);
    billingId = beanType.expressionPath("billingAddress.id");
    line1 = beanType.expressionPath("billingAddress.line1");
    city = beanType.expressionPath("billingAddress.city");
  }

  @Test
  public void pathSet() {

    Customer c1 = new Customer();
    line1.pathSet(c1, "12 someplace");
    city.pathSet(c1, "Auckland");
    billingId.pathSet(c1, 4);

    beanType.expressionPath("id").pathSet(c1, 42L);
    beanType.expressionPath("name").pathSet(c1, "jimmy");
    beanType.expressionPath("status").pathSet(c1, "ACTIVE");
    beanType.expressionPath("billingAddress.country.code").pathSet(c1, "NZ");
    beanType.expressionPath("billingAddress.country.name").pathSet(c1, "New Zealand");


    assertEquals(c1.getId(), Integer.valueOf(42));
    assertEquals(c1.getName(), "jimmy");
    assertEquals(c1.getStatus(), Customer.Status.ACTIVE);
    assertEquals(c1.getBillingAddress().getLine1(), "12 someplace");
    assertEquals(c1.getBillingAddress().getCity(), "Auckland");
    assertEquals(c1.getBillingAddress().getId(), Integer.valueOf("4"));

    assertEquals(c1.getBillingAddress().getCountry().getCode(), "NZ");
    assertEquals(c1.getBillingAddress().getCountry().getName(), "New Zealand");
  }

  @Test
  public void pathGet() {

    Address billingAddress = new Address();
    billingAddress.setId(12);
    billingAddress.setLine1("line1");
    billingAddress.setCity("Auckland");

    Country nz = new Country();
    nz.setCode("NZ");
    nz.setName("New Zealand");
    billingAddress.setCountry(nz);

    Customer c0 = new Customer();
    c0.setBillingAddress(billingAddress);

    EntityBean e0 = (EntityBean) c0;
    assertEquals(line1.pathGet(e0), "line1");
    assertEquals(city.pathGet(e0), "Auckland");
    assertEquals(billingId.pathGet(e0), Integer.valueOf("12"));


    assertEquals(beanType.expressionPath("billingAddress.country.code").pathGet(e0), "NZ");
    assertEquals(beanType.expressionPath("billingAddress.country.name").pathGet(e0), "New Zealand");
  }
}
