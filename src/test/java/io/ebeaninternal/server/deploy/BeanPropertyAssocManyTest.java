package io.ebeaninternal.server.deploy;

import io.ebean.BaseTestCase;
import io.ebean.bean.BeanCollection;
import io.ebean.bean.EntityBean;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;


public class BeanPropertyAssocManyTest extends BaseTestCase {

  BeanDescriptor<Customer> customerDesc = spiEbeanServer().getBeanDescriptor(Customer.class);

  @SuppressWarnings("unchecked")
  BeanPropertyAssocMany<Customer> contacts() {
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
