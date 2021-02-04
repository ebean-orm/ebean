package org.tests.batchload;

import io.ebean.BaseTestCase;
import io.ebean.BeanState;
import io.ebean.DB;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;


public class TestBeanState extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    List<Customer> custs = DB.find(Customer.class).findList();

    Customer customer = DB.find(Customer.class).setId(custs.get(0).getId()).select("name")
      .setUseCache(false)
      .findOne();

    BeanState beanState = DB.getBeanState(customer);
    assertFalse(beanState.isNew());
    assertFalse(beanState.isDirty());
    assertFalse(beanState.isNewOrDirty());
    assertNotNull(beanState.getLoadedProps());

    customer.setName("dirtyNameProp");
    assertTrue(beanState.isDirty());
    assertThat(beanState.getChangedProps()).containsOnly("name");

    EntityBeanIntercept ebi = ((EntityBean) customer)._ebean_getIntercept();
    boolean[] dirtyProperties = ebi.getDirtyProperties();
    for (int i = 0; i < dirtyProperties.length; i++) {
      if (dirtyProperties[i]) {
        String dirtyPropertyName = ebi.getProperty(i);
        assertEquals("name", dirtyPropertyName);
      }
    }

    customer.setStatus(Customer.Status.INACTIVE);

    assertTrue(beanState.isDirty());
    assertThat(beanState.getChangedProps()).containsOnly("name", "status");
  }

  @Test
  public void setDisableLazyLoad_expect_lazyLoadingDisabled() {

    ResetBasicData.reset();

    List<Customer> custs = DB.find(Customer.class).order("id").findList();

    Customer customer = DB.find(Customer.class)
      .setId(custs.get(0).getId())
      .select("id")
      .setUseCache(false)
      .findOne();

    BeanState beanState = DB.getBeanState(customer);
    beanState.setDisableLazyLoad(true);
    assertNull(customer.getName());
  }

  @Test
  public void getChangedProps_when_setManyProperty() {

    ResetBasicData.reset();

    Customer customer = DB.find(Customer.class).order("id").setMaxRows(1).findOne();

    BeanState beanState = DB.getBeanState(customer);
    assertThat(beanState.getChangedProps()).isEmpty();

    customer.setContacts(new ArrayList<>());
    assertThat(beanState.getChangedProps()).containsOnly("contacts");
  }

  @Test
  public void getChangedProps_when_setManyProperty_onNewBean() {

    Customer customer = new Customer();

    BeanState beanState = DB.getBeanState(customer);
    assertThat(beanState.getChangedProps()).isEmpty();

    // when new state, then loaded
    customer.setContacts(new ArrayList<>());
    assertThat(beanState.getChangedProps()).isEmpty();
    assertThat(beanState.getLoadedProps()).containsOnly("contacts");

    // set loaded state, then marked as changed
    beanState.setLoaded();
    customer.setContacts(new ArrayList<>());
    assertThat(beanState.getLoadedProps()).containsOnly("contacts");
    assertThat(beanState.getChangedProps()).containsOnly("contacts");
  }

  @Test(expected = IllegalStateException.class)
  public void readOnly_when_setManyProperty() {

    Customer customer = new Customer();
    customer.setContacts(new ArrayList<>());

    BeanState beanState = DB.getBeanState(customer);
    beanState.setLoaded();
    beanState.setReadOnly(true);

    // act, try to mutate read only bean
    customer.setContacts(new ArrayList<>());
  }


  @Test(expected = IllegalStateException.class)
  public void readOnly_when_setProperty() {

    Customer customer = new Customer();
    customer.setName("a");

    BeanState beanState = DB.getBeanState(customer);
    beanState.setLoaded();
    beanState.setReadOnly(true);

    // act, try to mutate read only bean
    customer.setName("b");
  }
}
