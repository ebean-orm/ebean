package org.tests.update;

import io.ebean.DB;
import io.ebean.TransactionalTestCase;
import io.ebeantest.LoggedSql;
import org.junit.Test;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;
import org.tests.model.basic.EBasic;
import org.tests.model.basic.EBasic.Status;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestStatelessUpdate extends TransactionalTestCase {

  @Test
  public void test() {

    EBasic e = new EBasic();
    e.setName("something");
    e.setStatus(Status.NEW);
    e.setDescription("wow");

    DB.save(e);

    // confirm saved as expected
    EBasic original = DB.find(EBasic.class, e.getId());
    assertEquals(e.getId(), original.getId());
    assertEquals(e.getName(), original.getName());
    assertEquals(e.getStatus(), original.getStatus());
    assertEquals(e.getDescription(), original.getDescription());

    // test updating just the name
    EBasic updateNameOnly = new EBasic();
    updateNameOnly.setId(e.getId());
    updateNameOnly.setName("updateNameOnly");

    LoggedSql.start();
    DB.update(updateNameOnly);

    List<String> sql = LoggedSql.collect();
    original = DB.find(EBasic.class, e.getId());
    assertEquals(e.getStatus(), original.getStatus());
    assertEquals(e.getDescription(), original.getDescription());
    assertEquals(updateNameOnly.getName(), original.getName());

    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("update e_basic set name=? where id=?; -- bind(updateNameOnly");

    LoggedSql.collect();
    // test setting null
    EBasic updateWithNull = new EBasic();
    updateWithNull.setId(e.getId());
    updateWithNull.setName("updateWithNull");
    updateWithNull.setDescription(null);
    DB.update(updateWithNull);

    sql = LoggedSql.stop();

    // name and description changed (using null)
    original = DB.find(EBasic.class, e.getId());
    assertEquals(e.getStatus(), original.getStatus());
    assertEquals(updateWithNull.getName(), original.getName());
    assertNull(original.getDescription());

    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("update e_basic set name=?, description=? where id=?; -- bind(updateWithNull,null,");
  }

  @Test(expected = EntityNotFoundException.class)
  public void update_NoRowsUpdated_expect_EntityNotFoundException() {

    EBasic basic = new EBasic();
    basic.setId(999999999);
    basic.setName("something");
    basic.setStatus(Status.ACTIVE);

    DB.update(basic);
  }

  @Test
  public void delete_NoRowsDeleted_expect_false() {

    EBasic basic = new EBasic();
    basic.setId(999999999);
    basic.setName("something");
    basic.setStatus(Status.ACTIVE);

    assertThat(DB.delete(basic)).isFalse();
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

    DB.save(basic);

    LoggedSql.start();
    // act
    EBasic basicWithoutChanges = new EBasic();
    basicWithoutChanges.setId(basic.getId());
    DB.update(basicWithoutChanges);

    // assert no update executed
    final List<String> sql = LoggedSql.stop();
    assertThat(sql).isEmpty();
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

    DB.save(customer);
    LoggedSql.start();

    // act
    Customer customerWithoutChanges = new Customer();
    customerWithoutChanges.setId(customer.getId());
    DB.update(customerWithoutChanges);
    final List<String> sql = LoggedSql.stop();

    Customer result = DB.find(Customer.class, customer.getId());
    assertThat(result.getUpdtime()).isEqualToIgnoringMillis(customer.getUpdtime());
    assertThat(sql).isEmpty();
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

    DB.save(customer);

    // act
    Customer customerWithChange = new Customer();
    customerWithChange.setId(customer.getId());
    customerWithChange.setName("new name");

    // contacts is not loaded
    assertFalse(containsContacts(customerWithChange));
    LoggedSql.start();
    DB.update(customerWithChange);
    final List<String> sql = LoggedSql.stop();

    Customer result = DB.find(Customer.class, customer.getId());

    // assert null list was ignored (missing children not deleted)
    assertNotNull(result.getContacts());
    assertFalse("the contacts mustn't be deleted", result.getContacts().isEmpty());
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("update o_customer set name=?, updtime=? where id=?;");
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

    DB.save(customer);

    // act
    Customer customerWithChange = new Customer();
    customerWithChange.setId(customer.getId());
    customerWithChange.setName("new name");

    // with Ebean enhancement this loads the an empty contacts BeanList
    customerWithChange.getContacts();

    // contacts has been initialised to empty BeanList
    assertTrue(containsContacts(customerWithChange));
    LoggedSql.start();
    DB.update(customerWithChange);
    final List<String> sql = LoggedSql.stop();

    Customer result = DB.find(Customer.class, customer.getId());

    // assert empty bean list was ignore (missing children not deleted)
    assertNotNull(result.getContacts());
    assertFalse("the contacts mustn't be deleted", result.getContacts().isEmpty());
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("update o_customer set name=?, updtime=? where id=?;");
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

    DB.save(customer);

    // act
    Customer customerWithChange = new Customer();
    customerWithChange.setId(customer.getId());
    customerWithChange.setName("new name");

    // with Ebean enhancement this loads the an empty contacts BeanList
    customerWithChange.setContacts(Collections.<Contact>emptyList());

    assertTrue(containsContacts(customerWithChange));
    LoggedSql.start();
    DB.update(customerWithChange);
    final List<String> sql = LoggedSql.stop();

    Customer result = DB.find(Customer.class, customer.getId());

    // assert empty bean list was ignore (missing children not deleted)
    assertThat(result.getContacts()).hasSize(1);
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("update o_customer set name=?, updtime=? where id=?;");
  }

  private boolean containsContacts(Customer cust) {
    return DB.getBeanState(cust).getLoadedProps().contains("contacts");
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

    DB.save(customer);

    // act
    Contact updateContact1 = new Contact();
    updateContact1.setId(contact1.getId());

    Contact updateContact2 = new Contact();
    updateContact2.setId(contact2.getId());

    Customer updateCustomer = new Customer();
    updateCustomer.setId(customer.getId());
    updateCustomer.getContacts().add(updateContact1);
    updateCustomer.getContacts().add(updateContact2);

    LoggedSql.start();
    DB.update(updateCustomer);
    final List<String> sql = LoggedSql.stop();

    // assert
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("update o_customer set updtime=? where id=?;");
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

    DB.save(customer);


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

    LoggedSql.start();
    DB.update(updateCustomer);
    final List<String> sql = LoggedSql.stop();

    assertThat(sql).hasSize(5);
    assertThat(sql.get(0)).contains("update o_customer set updtime=? where id=?");
    assertThat(sql.get(1)).contains("insert into contact");
    assertThat(sql.get(2)).contains(" -- bind(");
    assertThat(sql.get(3)).contains("update contact set last_name=?, customer_id=? where id=?");
    assertThat(sql.get(4)).contains(" -- bind(");

    // assert
    Customer assCustomer = DB.find(Customer.class, customer.getId());
    List<Contact> assContacts = assCustomer.getContacts();
    assertThat(assContacts).hasSize(3);
    Set<Integer> ids = new LinkedHashSet<>();
    Set<String> names = new LinkedHashSet<>();
    for (Contact contact : assContacts) {
      ids.add(contact.getId());
      names.add(contact.getLastName());
    }
    assertThat(ids).contains(contact1.getId(), updateContact3.getId(), contact2.getId());
    assertThat(names).contains(updateContact1.getLastName(), updateContact3.getLastName());
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

    DB.save(customer);

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
    LoggedSql.start();
    DB.update(updateCustomer);
    final List<String> sql = LoggedSql.stop();

    assertThat(sql).hasSize(5);
    assertThat(sql.get(0)).contains("update o_customer set updtime=? where id=?");
    assertThat(sql.get(1)).contains("insert into contact");
    assertThat(sql.get(2)).contains(" -- bind(");
    assertThat(sql.get(3)).contains("update contact set last_name=?, customer_id=? where id=?");
    assertThat(sql.get(4)).contains(" -- bind(");

    // assert
    Customer assCustomer = DB.find(Customer.class, customer.getId());
    List<Contact> assContacts = assCustomer.getContacts();

    // contact 2 was not deleted this time
    assertEquals(3, assContacts.size());

    Set<Integer> ids = new LinkedHashSet<>();
    Set<String> names = new LinkedHashSet<>();
    for (Contact contact : assContacts) {
      ids.add(contact.getId());
      names.add(contact.getLastName());
    }
    assertThat(ids).contains(contact1.getId(), updateContact3.getId(), contact2.getId());
    assertThat(names).contains(updateContact1.getLastName(), contact2.getLastName(), updateContact3.getLastName());
  }
}
