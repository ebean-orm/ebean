package org.tests.el;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanFkeyProperty;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import io.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import io.ebeaninternal.server.el.ElPropertyChain;
import io.ebeaninternal.server.el.ElPropertyDeploy;
import org.tests.model.basic.Customer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestELBasic extends BaseTestCase {

  @Test
  public void testEl() {

    SpiEbeanServer server = (SpiEbeanServer) Ebean.getServer(null);
    BeanDescriptor<Customer> descriptor = server.getBeanDescriptor(Customer.class);

    ElPropertyDeploy elId = descriptor.getElPropertyDeploy("id");
    assertTrue(elId instanceof BeanProperty);

    ElPropertyDeploy elBillAddress = descriptor.getElPropertyDeploy("billingAddress");
    assertTrue(elBillAddress instanceof BeanPropertyAssocOne<?>);

    ElPropertyDeploy elBillAddressId = descriptor.getElPropertyDeploy("billingAddress.id");
    assertTrue(elBillAddressId instanceof BeanFkeyProperty);
    assertEquals("billing_address_id", elBillAddressId.getDbColumn());
    assertEquals("billingAddress.id", elBillAddressId.getName());
    assertNull(elBillAddressId.getElPrefix());


    ElPropertyDeploy elBillAddressCity = descriptor.getElPropertyDeploy("billingAddress.city");
    assertTrue(elBillAddressCity instanceof ElPropertyChain);
    assertEquals("billingAddress", elBillAddressCity.getElPrefix());
    assertEquals("city", elBillAddressCity.getName());
    assertEquals("${billingAddress}city", elBillAddressCity.getElPlaceholder(false));
    assertEquals("city", elBillAddressCity.getDbColumn());

//		ElPropertyDeploy elBillAddressCountry = descriptor.getElPropertyDeploy("billingAddress.country");


    ElPropertyDeploy elOrders = descriptor.getElPropertyDeploy("orders");
    assertTrue(elOrders instanceof BeanPropertyAssocMany<?>);

    ElPropertyDeploy elOrderStatus = descriptor.getElPropertyDeploy("orders.status");
    assertTrue(elOrderStatus instanceof ElPropertyChain);
    assertEquals("orders", elOrderStatus.getElPrefix());
    assertEquals("status", elOrderStatus.getName());
    assertEquals("${orders}status", elOrderStatus.getElPlaceholder(false));
    assertEquals("status", elOrderStatus.getDbColumn());

    ElPropertyDeploy elOrderCust = descriptor.getElPropertyDeploy("orders.customer");
    assertTrue(elOrderCust instanceof ElPropertyChain);

    ElPropertyDeploy elOrderDetails = descriptor.getElPropertyDeploy("orders.details");
    assertTrue(elOrderDetails instanceof ElPropertyChain);

    ElPropertyDeploy elOrderDetailsId = descriptor.getElPropertyDeploy("orders.details.id");
    assertTrue(elOrderDetailsId instanceof ElPropertyChain);

  }


}
