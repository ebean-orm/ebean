package io.ebeaninternal.server.deploy;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebeaninternal.api.SpiEbeanServer;
import org.tests.model.basic.Customer;
import org.tests.model.basic.EBasic;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class BeanDescriptor_whenCreatedPropertyTest extends BaseTestCase {


  @Test
  public void test() {

    SpiEbeanServer server = (SpiEbeanServer) Ebean.getDefaultServer();

    BeanDescriptor<Customer> desc = server.getBeanDescriptor(Customer.class);

    BeanProperty whenCreatedProperty = desc.getWhenCreatedProperty();
    assertEquals("cretime", whenCreatedProperty.getDbColumn());

    BeanProperty whenModifiedProperty = desc.getWhenModifiedProperty();
    assertEquals("updtime", whenModifiedProperty.getDbColumn());


    BeanDescriptor<EBasic> eBasicDesc = server.getBeanDescriptor(EBasic.class);
    assertNull(eBasicDesc.getWhenCreatedProperty());
    assertNull(eBasicDesc.getWhenModifiedProperty());
  }
}
