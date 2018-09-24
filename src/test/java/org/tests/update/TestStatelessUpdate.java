package org.tests.update;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.TransactionalTestCase;

import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;
import org.tests.model.basic.EBasic;
import org.tests.model.basic.EBasic.Status;
import org.junit.Assert;
import org.junit.Test;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestStatelessUpdate extends TransactionalTestCase {

  private EbeanServer server = server();

  @Test
  public void test() {

    EBasic e = new EBasic();
    e.setName("something");
    e.setStatus(Status.NEW);
    e.setDescription("wow");

    server.save(e);

    // confirm saved as expected
    EBasic eBasic = server.find(EBasic.class, e.getId());
    assertEquals(e.getId(), eBasic.getId());
    assertEquals(e.getName(), eBasic.getName());
    assertEquals(e.getStatus(), eBasic.getStatus());
    assertEquals(e.getDescription(), eBasic.getDescription());

    // test updating just the name
    EBasic updateAll = new EBasic();
    updateAll.setId(e.getId());
    updateAll.setName("updAllProps");

    server.update(updateAll, null, false);

    eBasic = server.find(EBasic.class, e.getId());
    assertEquals(e.getStatus(), eBasic.getStatus());
    assertEquals(e.getDescription(), eBasic.getDescription());
    assertEquals(updateAll.getName(), eBasic.getName());


    // test setting null
    EBasic updateDeflt = new EBasic();
    updateDeflt.setId(e.getId());
    updateDeflt.setName("updateDeflt");
    updateDeflt.setDescription(null);
    server.update(updateDeflt);

    // name and description changed (using null)
    eBasic = server.find(EBasic.class, e.getId());
    assertEquals(e.getStatus(), eBasic.getStatus());
    assertEquals(updateDeflt.getName(), eBasic.getName());
    assertNull(eBasic.getDescription());

  }

  @Test(expected = EntityNotFoundException.class)
  public void update_NoRowsUpdated_expect_EntityNotFoundException() {

    EBasic basic = new EBasic();
    basic.setId(999999999);
    basic.setName("something");
    basic.setStatus(Status.ACTIVE);

    Ebean.update(basic);
  }

  @Test
  public void delete_NoRowsDeleted_expect_false() {

    EBasic basic = new EBasic();
    basic.setId(999999999);
    basic.setName("something");
    basic.setStatus(Status.ACTIVE);

    assertThat(Ebean.delete(basic)).isFalse();
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
    server.update(basicWithoutChanges);

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
   * see: {@link org.tests.update.TestUpdatePartial#testWithoutChangesAndVersionColumn()}
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
    server.update(customerWithoutChanges);

    Customer result = Ebean.find(Customer.class, customer.getId());

    // assert
    assertThat(result.getUpdtime()).isEqualToIgnoringMillis(customer.getUpdtime());
  }

  /**
   * Many relations mustn't be deleted when they are not loaded.
   */
  @Test
  public void testStatelessUpdateIgnoreNullCollection() {

    // arrange
    Contact contact = new Contact();
    contact.setFirstName("wobu :P");

    Customer customer = new Customer();
    customer.setName("something");
    customer.setContacts(new ArrayList<>());
    customer.getContacts().add(contact);

    server.save(customer);

    // act
    Customer customerWithChange = new Customer();
    customerWithChange.setId(customer.getId());
    customerWithChange.setName("new name");

    // contacts is not loaded
    Assert.assertFalse(containsContacts(customerWithChange));
    server.update(customerWithChange);

    Customer result = Ebean.find(Customer.class, customer.getId());

    // assert null list was ignored (missing children not deleted)
    Assert.assertNotNull(result.getContacts());
    Assert.assertFalse("the contacts mustn't be deleted", result.getContacts().isEmpty());
  }

  /**
   * When BeanCollection is inadvertantly initialised and empty then ignore it
   * Specifically a non-BeanCollection (like ArrayList) is not ignored in terms
   * of deleting missing children.
   */
  @Test
  public void testStatelessUpdateIgnoreEmptyBeanCollection() {

    // arrange
    Contact contact = new Contact();
    contact.setFirstName("wobu :P");

    Customer customer = new Customer();
    customer.setName("something");
    customer.setContacts(new ArrayList<>());
    customer.getContacts().add(contact);

    server.save(customer);

    // act
    Customer customerWithChange = new Customer();
    customerWithChange.setId(customer.getId());
    customerWithChange.setName("new name");

    // with Ebean enhancement this loads the an empty contacts BeanList
    customerWithChange.getContacts();

    // contacts has been initialised to empty BeanList
    Assert.assertTrue(containsContacts(customerWithChange));
    server.update(customerWithChange);

    Customer result = Ebean.find(Customer.class, customer.getId());

    // assert empty bean list was ignore (missing children not deleted)
    Assert.assertNotNull(result.getContacts());
    Assert.assertFalse("the contacts mustn't be deleted", result.getContacts().isEmpty());
  }

  @Test
  public void testStatelessUpdateDeleteChildrenForNonBeanCollection() {

    // arrange
    Contact contact = new Contact();
    contact.setFirstName("wobu :P");

    Customer customer = new Customer();
    customer.setName("something");
    customer.setContacts(new ArrayList<>());
    customer.getContacts().add(contact);

    server.save(customer);

    // act
    Customer customerWithChange = new Customer();
    customerWithChange.setId(customer.getId());
    customerWithChange.setName("new name");

    // with Ebean enhancement this loads the an empty contacts BeanList
    customerWithChange.setContacts(Collections.<Contact>emptyList());

    Assert.assertTrue(containsContacts(customerWithChange));
    server.update(customerWithChange);

    Customer result = Ebean.find(Customer.class, customer.getId());

    // assert empty bean list was ignore (missing children not deleted)
    Assert.assertNotNull(result.getContacts());
    Assert.assertTrue("the contacts were deleted", result.getContacts().isEmpty());
  }

  private boolean containsContacts(Customer cust) {
    return server.getBeanState(cust).getLoadedProps().contains("contacts");
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
    contact2.setLastName("contact2");

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

    server.update(updateCustomer);

    // assert
    // maybe check if update instead of insert has been executed,
    // currently "Unique index or primary key violation" PersistenceException is throwing
  }

  @Test
  public void testStatelessRecursiveUpdateWithChangesInDetailOnly() {
    // arrange
    Contact contact1 = new Contact();
    contact1.setLastName("contact1");

    Contact contact2 = new Contact();
    contact2.setLastName("contact2");

    Customer customer = new Customer();
    customer.setName("something");
    customer.getContacts().add(contact1);
    customer.getContacts().add(contact2);

    server.save(customer);


    // act
    Contact updateContact1 = new Contact();
    updateContact1.setId(contact1.getId());
    updateContact1.setLastName("contact1-changed");


    Contact updateContact3 = new Contact();
    //updateContact3.setId(contact3.getId());
    updateContact3.setLastName("contact3-added");

    Customer updateCustomer = new Customer();
    updateCustomer.setId(customer.getId());
    updateCustomer.getContacts().add(updateContact1);
    updateCustomer.getContacts().add(updateContact3);

    // not adding contact2 so it will get deleted
    //updateCustomer.getContacts().add(updateContact2);

    server.update(updateCustomer);


    // assert
    Customer assCustomer = server.find(Customer.class, customer.getId());
    List<Contact> assContacts = assCustomer.getContacts();
    assertEquals(2, assContacts.size());
    Set<Integer> ids = new LinkedHashSet<>();
    Set<String> names = new LinkedHashSet<>();
    for (Contact contact : assContacts) {
      ids.add(contact.getId());
      names.add(contact.getLastName());
    }
    Assert.assertTrue(ids.contains(contact1.getId()));
    Assert.assertTrue(ids.contains(updateContact3.getId()));
    Assert.assertFalse(ids.contains(contact2.getId()));

    Assert.assertTrue(names.contains(updateContact1.getLastName()));
    Assert.assertTrue(names.contains(updateContact3.getLastName()));
  }


  @Test
  public void testStatelessRecursiveUpdateWithChangesInDetailOnlyAnd() {
    // arrange
    Contact contact1 = new Contact();
    contact1.setLastName("contact1");

    Contact contact2 = new Contact();
    contact2.setLastName("contact2");

    Customer customer = new Customer();
    customer.setName("something");
    customer.getContacts().add(contact1);
    customer.getContacts().add(contact2);

    server.save(customer);


    // act
    Contact updateContact1 = new Contact();
    updateContact1.setId(contact1.getId());
    updateContact1.setLastName("contact1-changed");


    Contact updateContact3 = new Contact();
    updateContact3.setLastName("contact3-added");

    Customer updateCustomer = new Customer();
    updateCustomer.setId(customer.getId());
    updateCustomer.getContacts().add(updateContact1);
    updateCustomer.getContacts().add(updateContact3);

    // not adding contact2 but it won't be deleted in this case
    boolean deleteMissingChildren = false;
    server.update(updateCustomer, null, deleteMissingChildren);


    // assert
    Customer assCustomer = server.find(Customer.class, customer.getId());
    List<Contact> assContacts = assCustomer.getContacts();

    // contact 2 was not deleted this time
    assertEquals(3, assContacts.size());

    Set<Integer> ids = new LinkedHashSet<>();
    Set<String> names = new LinkedHashSet<>();
    for (Contact contact : assContacts) {
      ids.add(contact.getId());
      names.add(contact.getLastName());
    }
    Assert.assertTrue(ids.contains(contact1.getId()));
    Assert.assertTrue(ids.contains(updateContact3.getId()));
    Assert.assertTrue(ids.contains(contact2.getId()));

    Assert.assertTrue(names.contains(updateContact1.getLastName()));
    Assert.assertTrue(names.contains(contact2.getLastName()));
    Assert.assertTrue(names.contains(updateContact3.getLastName()));
  }
}
