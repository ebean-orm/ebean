package io.ebeaninternal.server.deploy;

import io.ebean.BaseTestCase;
import io.ebean.bean.BeanCollection;
import io.ebean.bean.EntityBean;
import io.ebean.common.BeanList;
import org.junit.Test;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


public class BeanPropertyAssocManyTest extends BaseTestCase {

  private BeanDescriptor<Customer> customerDesc = spiEbeanServer().getBeanDescriptor(Customer.class);

  @SuppressWarnings("unchecked")
  private BeanPropertyAssocMany<Customer> contacts() {
    return (BeanPropertyAssocMany<Customer>) customerDesc.getBeanProperty("contacts");
  }

  @Test
  public void createReferenceIfNull_when_notBeanCollection_expect_null() {

    Customer customer = new Customer();
    customer.setContacts(new ArrayList<>());

    BeanCollection<?> ref = contacts().createReferenceIfNull((EntityBean) customer);
    assertNull(ref);
  }

  @Test
  public void createReferenceIfNull_when_null_expect_ref() {

    Customer customer = new Customer();
    customer.setContacts(null);

    BeanCollection<?> ref = contacts().createReferenceIfNull((EntityBean) customer);
    assertNotNull(ref);
    assertTrue(ref.isReference());
  }

  @Test
  public void lazyLoadMany_addsToParentCollection() {

    Customer customer = new Customer();
    customer.setContacts(new BeanList<>());

    Contact contact = new Contact();
    contact.setCustomer(customer);

    contacts().lazyLoadMany((EntityBean) contact);

    assertThat(customer.getContacts()).hasSize(1);
    assertThat(customer.getContacts().get(0)).isSameAs(contact);
  }

  @Test
  public void findIdsByParentId() {

    ResetBasicData.reset();

    List<Object> customerIds = new ArrayList<>();
    customerIds.add(1L);
    customerIds.add(2L);

    List<Object> contactIdsForOne = contacts().findIdsByParentId(1L, null, null, null);

    List<Object> contactIdsForMultiple = contacts().findIdsByParentId(null, customerIds, null, null);

    assertThat(contactIdsForOne).isNotEmpty();
    assertThat(contactIdsForMultiple).isNotEmpty();
  }
}
