package com.avaje.tests.update;

import com.avaje.tests.model.basic.Contact;
import com.avaje.tests.model.basic.Customer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.tests.model.basic.EBasic;
import com.avaje.tests.model.basic.EBasic.Status;

import java.util.ArrayList;

public class TestStatelessUpdate extends BaseTestCase {

  private EbeanServer server;

  @Before
  public void setUp() {
    server = Ebean.getServer(null);
  }

  @Test
  public void test() {

    // GlobalProperties.put("ebean.defaultUpdateNullProperties", "true");
    // GlobalProperties.put("ebean.defaultDeleteMissingChildren", "false");

    EBasic e = new EBasic();
    e.setName("something");
    e.setStatus(Status.NEW);
    e.setDescription("wow");

    server.save(e);

    // EBasic updateName = new EBasic();
    // updateName.setId(e.getId());
    // updateName.setName("justName");
    //
    //
    // server.update(updateName, null, null, false, false);

    EBasic updateAll = new EBasic();
    updateAll.setId(e.getId());
    updateAll.setName("updAllProps");

    server.update(updateAll, null, null, false, true);

    EBasic updateDeflt = new EBasic();
    updateDeflt.setId(e.getId());
    updateDeflt.setName("updateDeflt");

    server.update(updateDeflt);

  }

  /**
   * I am expecting that Ebean detects there aren't any changes and don't execute any query.
   * Currently a {@link javax.persistence.PersistenceException} with message 'Invalid value "null" for parameter "SQL"' is thrown.
   */
  @Test
  public void testWithoutChangesAndIgnoreNullValues() {
    // arrange
    EBasic basic = new EBasic();
    basic.setName("something");
    basic.setStatus(Status.NEW);
    basic.setDescription("wow");

    server.save(basic);

    // act
    EBasic basicWithoutChanges = new EBasic();
    basicWithoutChanges.setId(basic.getId());
    server.update(basicWithoutChanges, null, null, true, false);

    // assert
    // Nothing to check, simply no exception should occur
    // maybe ensure that no update has been executed
  }

  /**
   * Nice to have:
   * <br />
   * Assuming we have a Version column, it will always be generated an Update despite we have nothing to update.
   * It would be nice that this would be recognized and no update would happen.
   * <br />
   * <br />
   * This feature already works for normal Updates!
   * <br />
   * see: {@link com.avaje.tests.update.TestUpdatePartial#testWithoutChangesAndVersionColumn()}
   */
  @Test
  public void testWithoutChangesAndVersionColumnAndIgnoreNullValues() {
    // arrange
    Customer customer = new Customer();
    customer.setName("something");

    server.save(customer);

    // act
    Customer customerWithoutChanges = new Customer();
    customerWithoutChanges.setId(customer.getId());
    server.update(customerWithoutChanges, null, null, true, false);

    Customer result = Ebean.find(Customer.class, customer.getId());

    // assert
    Assert.assertEquals(customer.getUpdtime().getTime(), result.getUpdtime().getTime());
  }

  /**
   * Many relations mustn't be deleted when having a {@link com.avaje.ebean.event.BeanPersistAdapter} which is accessing this many field.
   */
  @Test
  public void testStatelessUpdateWithPersistAdapterAndIgnoreNullValues() {

    // arrange
    Contact contact = new Contact();
    contact.setFirstName("wobu :P");

    Customer customer = new Customer();
    customer.setName("something");
    customer.getContacts().add(contact);

    server.save(customer);

    // act
    Customer customerWithChange = new Customer();
    customerWithChange.setId(customer.getId());
    customerWithChange.setName("new name");

    server.update(customerWithChange, null, null, true, false);

    Customer result = Ebean.find(Customer.class, customer.getId());

    // assert
    Assert.assertNotNull(result.getContacts());
    Assert.assertFalse("the contacts mustn't be deleted", result.getContacts().isEmpty());
  }

  /**
   * when using stateless updates with recursive calls,
   * the version column shouldn't decide to use insert instead of update,
   * although an ID has been set.
   */
  @Test
  public void testStatelessRecursiveUpdateWithVersionField() {
    // arrange
    Contact contact1 = new Contact();
    contact1.setLastName("contact1");

    Contact contact2 = new Contact();
    contact1.setLastName("contact2");

    Customer customer = new Customer();
    customer.setName("something");
    customer.getContacts().add(contact1);
    customer.getContacts().add(contact2);

    server.save(customer);

    // act
    Contact updateContact1 = new Contact();
    updateContact1.setId(contact1.getId());

    Contact updateContact2 = new Contact();
    updateContact2.setId(contact2.getId());

    Customer updateCustomer = new Customer();
    updateCustomer.setId(customer.getId());
    updateCustomer.getContacts().add(updateContact1);
    updateCustomer.getContacts().add(updateContact2);

    server.update(updateCustomer, null, null, true, false);

    // assert
    // nothing to do, simply no exception should happen
    // maybe check if update instead of insert has been executed,
  }
}
