package org.tests.basic;

import io.ebean.DB;
import io.ebean.Update;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testcase identified a bug when collections are used as bind parameters
 *
 * @author Roland Praml, FOCONIS AG
 */
public class TestUpdate extends BaseTestCase {

  @BeforeEach
  public void createCustomers() {
    ResetBasicData.reset();
    for (int i = 1; i <= 3; i++) {
      Customer cust = new Customer();
      cust.setName("testUpdate" + i);
      DB.save(cust);
    }
  }

  @AfterEach
  public void deleteCustomers() {
    DB.createUpdate(Customer.class, "delete from customer where name like 'testUpdate%'").execute();
  }

  @Test
  public void testNormal() {
    String sillyContent = "a123456789b123456789c123456789d123456789e123456789";
    for (int i = 1; i <= 3; i++) {
      Update<Customer> update = DB.createUpdate(Customer.class,
        "update customer set smallnote = :smallnote where name in (:name)");
      update
        .setParameter("name", List.of("testUpdate" + i))
        .setParameter("smallnote", "Note #" + i + sillyContent)
        .execute();
    }
    Customer cust = DB.find(Customer.class).where().eq("name", "testUpdate3").findOne();
    assertThat(cust.getSmallnote()).isEqualTo("Note #3" + sillyContent);
  }

  @Test
  public void testReuse() {
    Update<Customer> update = DB.createUpdate(Customer.class,
      "update customer set smallnote = :smallnote where name in (:name)");
    for (int i = 1; i <= 3; i++) {
      update.setParameter("name", Arrays.asList("testUpdate" + i)).setParameter("smallnote", "Note #" + i).execute();
    }
    Customer cust = DB.find(Customer.class).where().eq("name", "testUpdate3").findOne();
    assertThat(cust.getSmallnote()).isEqualTo("Note #3");
  }

  @Test
  public void testReuseNoArray() {
    Update<Customer> update = DB.createUpdate(Customer.class,
      "update customer set smallnote = :smallnote where name = :name");
    for (int i = 1; i <= 3; i++) {
      update.setParameter("name", "testUpdate" + i).setParameter("smallnote", "Note #" + i).execute();
    }
    Customer cust = DB.find(Customer.class).where().eq("name", "testUpdate3").findOne();
    assertThat(cust.getSmallnote()).isEqualTo("Note #3");
  }
}
