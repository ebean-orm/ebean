package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.EBasic;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class BeanDescriptor_whenCreatedPropertyTest extends BaseTestCase {


  @Test
  public void test() {

    SpiEbeanServer server = (SpiEbeanServer)Ebean.getDefaultServer();

    BeanDescriptor<Customer> desc = server.getBeanDescriptor(Customer.class);

    BeanProperty whenCreatedProperty = desc.findWhenCreatedProperty();
    assertEquals("cretime",whenCreatedProperty.getDbColumn());

    BeanProperty whenModifiedProperty = desc.findWhenModifiedProperty();
    assertEquals("updtime",whenModifiedProperty.getDbColumn());


    BeanDescriptor<EBasic> eBasicDesc = server.getBeanDescriptor(EBasic.class);
    assertNull(eBasicDesc.findWhenCreatedProperty());
    assertNull(eBasicDesc.findWhenModifiedProperty());
  }
}