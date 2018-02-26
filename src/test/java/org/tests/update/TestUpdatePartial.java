package org.tests.update;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.SqlQuery;
import io.ebean.SqlRow;
import org.tests.model.basic.Customer;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class TestUpdatePartial extends BaseTestCase {

  @Test
  public void test() {

    Customer c = new Customer();
    c.setName("TestUpdateMe");
    c.setStatus(Customer.Status.ACTIVE);
    c.setSmallnote("a note");

    Ebean.save(c);
    checkDbStatusValue(c.getId(), "A");

    Customer c2 = Ebean.find(Customer.class)
      .setUseCache(false)
      .select("status, smallnote")
      .setId(c.getId())
      .findOne();

    c2.setStatus(Customer.Status.INACTIVE);
    c2.setSmallnote("2nd note");

    Ebean.save(c2);
    checkDbStatusValue(c.getId(), "I");

    Customer c3 = Ebean.find(Customer.class)
      .setUseCache(false)
      .select("status")
      .setId(c.getId())
      .findOne();

    c3.setStatus(Customer.Status.NEW);
    c3.setSmallnote("3rd note");

    Ebean.save(c3);
    checkDbStatusValue(c.getId(), "N");
  }

  private void checkDbStatusValue(Integer custId, String dbStatus) {
    SqlQuery sqlQuery = Ebean.createSqlQuery("select id, status from o_customer where id = ?");
    sqlQuery.setParameter(1, custId);
    SqlRow sqlRow = sqlQuery.findOne();
    String status = sqlRow.getString("status");
    assertEquals(dbStatus, status);
  }

  /**
   * If we have no changes detected, don't execute an Update and don't update the Version column.
   */
  @Test
  public void testWithoutChangesAndVersionColumn() {

    Customer customer = new Customer();
    customer.setName("something");

    Ebean.save(customer);

    Customer customerWithoutChanges = Ebean.find(Customer.class, customer.getId());
    Ebean.save(customerWithoutChanges);

    assertThat(customerWithoutChanges.getUpdtime()).isEqualToIgnoringMillis(customer.getUpdtime());
  }
}
