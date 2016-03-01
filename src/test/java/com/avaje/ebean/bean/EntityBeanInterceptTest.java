package com.avaje.ebean.bean;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.EBasic;
import com.avaje.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.sql.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EntityBeanInterceptTest extends BaseTestCase {

  @Test
  public void testHasDirtyProperty() throws Exception {


    ResetBasicData.reset();

    List<Customer> list = Ebean.find(Customer.class).findList();

    Set<String> propertyNames = new HashSet<String>();
    propertyNames.add("name");
    propertyNames.add("status");


    Customer customer = list.get(0);
    EntityBeanIntercept ebi = ((EntityBean)customer)._ebean_getIntercept();

    assertFalse(ebi.hasDirtyProperty(propertyNames));

    customer.setAnniversary(new Date(System.currentTimeMillis()));
    assertFalse(ebi.hasDirtyProperty(propertyNames));

    customer.setStatus(Customer.Status.ACTIVE);
    assertTrue(ebi.hasDirtyProperty(propertyNames));

  }

  @Test
  public void isPartial_when_new() {

    EBasic basic = new EBasic();
    EntityBeanIntercept ebi = ((EntityBean) basic)._ebean_getIntercept();
    assertThat(ebi.isPartial()).isTrue();
  }

  @Test
  public void isPartial_when_partial() {

    EBasic basic = new EBasic();
    basic.setId(42);
    basic.setName("some");
    EntityBeanIntercept ebi = ((EntityBean) basic)._ebean_getIntercept();
    assertThat(ebi.isPartial()).isTrue();
  }

  @Test
  public void isPartial_when_full() {

    EBasic basic = new EBasic();
    basic.setId(42);
    basic.setName("some");
    basic.setDescription("asd");
    basic.setSomeDate(null);
    basic.setStatus(EBasic.Status.ACTIVE);

    EntityBeanIntercept ebi = ((EntityBean) basic)._ebean_getIntercept();
    assertThat(ebi.isPartial()).isFalse();
  }

}