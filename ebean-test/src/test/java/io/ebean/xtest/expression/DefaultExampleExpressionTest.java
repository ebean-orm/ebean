package io.ebean.xtest.expression;

import io.ebean.Query;
import io.ebean.xtest.BaseTestCase;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Address;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import static org.assertj.core.api.Assertions.assertThat;


public class DefaultExampleExpressionTest extends BaseTestCase {

  Customer customer() {
    Address address = new Address();
    address.setCity("billingAddress.city");

    Customer customer = new Customer();
    customer.setName("name");
    customer.setBillingAddress(address);
    return customer;
  }

  @Test
  public void test() {

    Customer customer = customer();
    customer.setName("Rob");
    customer.getBillingAddress().setCity("Auckland");

    ResetBasicData.reset();

    Query<Customer> query1 = server().find(Customer.class)
      .where().exampleLike(customer)
      .query();

    query1.findList();

    assertThat(query1.getGeneratedSql()).contains("(t0.name like ");
    assertThat(query1.getGeneratedSql()).contains(" and t1.city like ");
  }

  @Test
  public void emptyExampleBean() {

    ResetBasicData.reset();

    Customer customer = new Customer();

    Query<Customer> query = server().find(Customer.class)
      .where()
      .eq("status", Customer.Status.NEW)
      .exampleLike(customer)
      .startsWith("name", "Rob")
      .query();

    query.findList();

    Assertions.assertThat(sqlOf(query)).contains("and 1=1 and t0.name like");
  }

}
