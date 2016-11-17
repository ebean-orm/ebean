package com.avaje.tests.batchload;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.BeanState;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestLoadOnDirty extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    List<Customer> custs = Ebean.find(Customer.class).findList();

    Customer customer = Ebean.find(Customer.class).setId(custs.get(0).getId()).select("name")
      .setUseCache(false)
      .findUnique();

    BeanState beanState = Ebean.getBeanState(customer);
    Assert.assertTrue(!beanState.isNew());
    Assert.assertTrue(!beanState.isDirty());
    Assert.assertTrue(!beanState.isNewOrDirty());
    Assert.assertNotNull(beanState.getLoadedProps());

    customer.setName("dirtyNameProp");
    Assert.assertTrue(beanState.isDirty());
    Assert.assertTrue(beanState.getChangedProps().contains("name"));
    Assert.assertEquals(1, beanState.getChangedProps().size());

    EntityBeanIntercept ebi = ((EntityBean) customer)._ebean_getIntercept();
    boolean[] dirtyProperties = ebi.getDirtyProperties();
    for (int i = 0; i < dirtyProperties.length; i++) {
      if (dirtyProperties[i]) {
        String dirtyPropertyName = ebi.getProperty(i);
        Assert.assertEquals("name", dirtyPropertyName);
      }
    }

    customer.setStatus(Customer.Status.INACTIVE);

    Assert.assertTrue(beanState.isDirty());
    Assert.assertTrue(beanState.getChangedProps().contains("status"));
    Assert.assertTrue(beanState.getChangedProps().contains("name"));
    Assert.assertEquals(2, beanState.getChangedProps().size());

  }

  @Test
  public void testDisableLazyLoad() {

    ResetBasicData.reset();

    List<Customer> custs = Ebean.find(Customer.class).order("id").findList();

    Customer customer = Ebean.find(Customer.class)
      .setId(custs.get(0).getId())
      .select("id")
      .setUseCache(false)
      .findUnique();

    BeanState beanState = Ebean.getBeanState(customer);
    beanState.setDisableLazyLoad(true);
    Assert.assertNull(customer.getName());
  }
}
