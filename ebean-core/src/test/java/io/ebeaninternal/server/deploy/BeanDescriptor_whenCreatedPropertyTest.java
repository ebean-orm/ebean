package io.ebeaninternal.server.deploy;

import io.ebean.DB;
import io.ebeaninternal.api.SpiEbeanServer;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.EBasic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class BeanDescriptor_whenCreatedPropertyTest {

  @Test
  public void test() {

    SpiEbeanServer server = (SpiEbeanServer) DB.getDefault();

    BeanDescriptor<Customer> desc = server.descriptor(Customer.class);

    BeanProperty whenCreatedProperty = desc.whenCreatedProperty();
    assertEquals("cretime", whenCreatedProperty.dbColumn());

    BeanProperty whenModifiedProperty = desc.whenModifiedProperty();
    assertEquals("updtime", whenModifiedProperty.dbColumn());


    BeanDescriptor<EBasic> eBasicDesc = server.descriptor(EBasic.class);
    assertNull(eBasicDesc.whenCreatedProperty());
    assertNull(eBasicDesc.whenModifiedProperty());
  }
}
