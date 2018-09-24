package org.tests.query;

import io.ebean.BaseTestCase;
import org.junit.Test;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestQueryFindNative extends BaseTestCase {


  @Test
  public void joinFromManyToOne() {

    ResetBasicData.reset();

    String sql =
      "select c.id, c.first_name, c.last_name, t.id, t.name " +
        " from contact c  " +
        " join o_customer t on t.id = c.customer_id " +
        " where t.name like ? " +
        " order by c.first_name, c.last_name";

    List<Contact> contacts =
      server()
        .findNative(Contact.class, sql)
        .setParameter(1, "Rob")
        .findList();


    assertThat(contacts).isNotEmpty();

    Customer customer = contacts.get(0).getCustomer();
    assertThat(customer).isNotNull();
  }

  @Test
  public void joinFromOneToMany() {

    ResetBasicData.reset();

    String sql =
      "select cu.id, cu.name, ct.id, ct.first_name " +
        " from o_customer cu " +
        " left join contact ct on cu.id = ct.customer_id " +
        " where cu.name like ? " +
        " order by name";

    List<Customer> customers =
      server()
        .findNative(Customer.class, sql)
        .setParameter(1, "Rob")
        .findList();

    assertThat(customers).isNotEmpty();

    List<Contact> contacts = customers.get(0).getContacts();
    assertThat(contacts).isNotEmpty();
  }
}
