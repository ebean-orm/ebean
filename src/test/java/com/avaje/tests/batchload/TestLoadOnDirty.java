package com.avaje.tests.batchload;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.BeanState;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

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

    customer.setStatus(Customer.Status.INACTIVE);

    Assert.assertTrue(beanState.isDirty());
    Assert.assertTrue(beanState.getChangedProps().contains("status"));
    Assert.assertTrue(beanState.getChangedProps().contains("name"));
    Assert.assertEquals(2, beanState.getChangedProps().size());

  }
}
