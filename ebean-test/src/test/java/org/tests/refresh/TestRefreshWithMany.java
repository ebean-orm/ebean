package org.tests.refresh;

import io.ebean.DB;
import io.ebean.SqlRow;
import io.ebean.TransactionalTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestRefreshWithMany extends TransactionalTestCase {

  @Test
  public void test() {


    Customer customer = ResetBasicData.createCustomer("refresher", "22 refresh set", "23 fresh", 9);

    DB.save(customer);

    assertEquals(3, customer.getContacts().size());

    int rc = DB.sqlUpdate("update o_customer set name = :name where id = :id")
      .setParameter("name", "ref-modified")
      .setParameter("id", customer.getId())
      .execute();

    int rc2 = DB.sqlUpdate("update contact set first_name = concat(first_name,'-mod') where customer_id = :id")
      .setParameter("id", customer.getId())
      .execute();

    assertEquals(1, rc);
    assertEquals(3, rc2);

    DB.refresh(customer);

    assertEquals("ref-modified", customer.getName());
    assertEquals(3, customer.getContacts().size());
    assertEquals(true, customer.getContacts().get(0).getFirstName().endsWith("-mod"));


    // now try again when the bean is fully loaded
    Customer customer1 = DB.find(Customer.class)
      .fetch("contacts")
      .setId(customer.getId())
      .findOne();


    int rcb1 = DB.sqlUpdate("update o_customer set name = :name where id = :id")
      .setParameter("name", "ref-modified-again")
      .setParameter("id", customer.getId())
      .execute();

    int rcb2 = DB.sqlUpdate("update contact set first_name = :first, last_name='foo' where customer_id = :id")
      .setParameter("id", customer.getId())
      .setParameter("first", "-alt")
      .execute();

    List<SqlRow> sqlRows = DB.sqlQuery("select id, first_name, last_name from contact").findList();
    for (SqlRow sqlRow : sqlRows) {
      sqlRow.get("id");
      sqlRow.get("first_name");
      sqlRow.get("last_name");
    }

    assertEquals(1, rcb1);
    assertEquals(3, rcb2);

    DB.refresh(customer1);

    assertEquals("ref-modified-again", customer1.getName());
    assertEquals(3, customer1.getContacts().size());
    for (Contact contact : customer1.getContacts()) {
      contact.getId();
      contact.getFirstName();
      contact.getLastName();
    }
    assertEquals(true, customer1.getContacts().get(0).getFirstName().endsWith("-alt"));

  }
}
