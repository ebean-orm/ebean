package org.tests.batchload;

import io.ebean.BeanState;
import io.ebean.DB;
import io.ebean.UnmodifiableEntityException;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


class TestBeanState extends BaseTestCase {

  @BeforeAll
  static void before() {
    ResetBasicData.reset();
  }

  @Test
  void invalid() {
    assertThrows(IllegalArgumentException.class, () -> DB.beanState(new Object()));
  }

  @Test
  void loadErrors_when_empty() {
    Customer one = DB.find(Customer.class).setMaxRows(1).findOne();
    BeanState beanState = DB.beanState(one);

    assertThat(beanState.loadErrors()).isEmpty();
  }

  @Test
  void test() {
    List<Customer> custs = DB.find(Customer.class).findList();

    Customer customer = DB.find(Customer.class).setId(custs.get(0).getId()).select("name")
      .setUseCache(false)
      .findOne();

    BeanState beanState = DB.beanState(customer);
    assertFalse(beanState.isNew());
    assertFalse(beanState.isDirty());
    assertFalse(beanState.isNewOrDirty());
    assertNotNull(beanState.loadedProps());

    customer.setName("dirtyNameProp");
    assertTrue(beanState.isDirty());
    assertThat(beanState.changedProps()).containsOnly("name");

    EntityBeanIntercept ebi = ((EntityBean) customer)._ebean_getIntercept();
    boolean[] dirtyProperties = ebi.dirtyProperties();
    for (int i = 0; i < dirtyProperties.length; i++) {
      if (dirtyProperties[i]) {
        String dirtyPropertyName = ebi.property(i);
        assertEquals("name", dirtyPropertyName);
      }
    }

    customer.setStatus(Customer.Status.INACTIVE);

    assertTrue(beanState.isDirty());
    assertThat(beanState.changedProps()).containsOnly("name", "status");
  }

  @Test
  void setDisableLazyLoad_expect_lazyLoadingDisabled() {
    List<Customer> custs = DB.find(Customer.class).orderBy("id").findList();

    Customer customer = DB.find(Customer.class)
      .setId(custs.get(0).getId())
      .select("id")
      .setUseCache(false)
      .findOne();

    BeanState beanState = DB.beanState(customer);
    beanState.setDisableLazyLoad(true);
    assertNull(customer.getName());
  }

  @Test
  void changedProps_when_setManyProperty() {

    Customer customer = DB.find(Customer.class).orderBy("id").setMaxRows(1).findOne();

    BeanState beanState = DB.beanState(customer);
    assertThat(beanState.changedProps()).isEmpty();

    customer.setContacts(new ArrayList<>());
    assertThat(beanState.changedProps()).containsOnly("contacts");
  }

  @Test
  void changedProps_when_setManyProperty_onNewBean() {
    Customer customer = new Customer();

    BeanState beanState = DB.beanState(customer);
    assertThat(beanState.changedProps()).isEmpty();

    // when new state, then loaded
    customer.setContacts(new ArrayList<>());
    assertThat(beanState.changedProps()).isEmpty();
    assertThat(beanState.loadedProps()).containsOnly("contacts");

    // set loaded state, then marked as changed
    beanState.setLoaded();
    customer.setContacts(new ArrayList<>());
    assertThat(beanState.loadedProps()).containsOnly("contacts");
    assertThat(beanState.changedProps()).containsOnly("contacts");
  }

  @Test
  void readOnly_when_setManyProperty() {
    Customer customer = new Customer();
    customer.setContacts(new ArrayList<>());

    BeanState beanState = DB.beanState(customer);
    beanState.setLoaded();
    beanState.setReadOnly(true);

    // act, try to mutate read only bean
    assertThrows(UnmodifiableEntityException.class, () -> customer.setContacts(new ArrayList<>()));
  }

  @Test
  void readOnly_when_setProperty() {
    Customer customer = new Customer();
    customer.setName("a");

    BeanState beanState = DB.beanState(customer);
    beanState.setLoaded();
    beanState.setReadOnly(true);

    // act, try to mutate read only bean
    assertThrows(UnmodifiableEntityException.class, () -> customer.setName("b"));
  }
}
