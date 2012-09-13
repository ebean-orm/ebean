package com.avaje.tests.el;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanFkeyProperty;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import com.avaje.ebeaninternal.server.el.ElPropertyChain;
import com.avaje.ebeaninternal.server.el.ElPropertyDeploy;
import com.avaje.tests.model.basic.Customer;

public class TestELBasic extends TestCase {

	public void testEl() {
		
		SpiEbeanServer server = (SpiEbeanServer)Ebean.getServer(null);
		BeanDescriptor<Customer> descriptor = server.getBeanDescriptor(Customer.class);
		
		ElPropertyDeploy elId = descriptor.getElPropertyDeploy("id");
		Assert.assertTrue(elId instanceof BeanProperty);
		
		ElPropertyDeploy elBillAddress = descriptor.getElPropertyDeploy("billingAddress");
		Assert.assertTrue(elBillAddress instanceof BeanPropertyAssocOne<?>);
		
		ElPropertyDeploy elBillAddressId = descriptor.getElPropertyDeploy("billingAddress.id");
		Assert.assertTrue(elBillAddressId instanceof BeanFkeyProperty);
		Assert.assertEquals("billing_address_id", elBillAddressId.getDbColumn());
		Assert.assertEquals("billingAddress.id", elBillAddressId.getName());
		Assert.assertNull(elBillAddressId.getElPrefix());
		
		
		ElPropertyDeploy elBillAddressCity = descriptor.getElPropertyDeploy("billingAddress.city");
		Assert.assertTrue(elBillAddressCity instanceof ElPropertyChain);
		Assert.assertEquals("billingAddress",elBillAddressCity.getElPrefix());
		Assert.assertEquals("city",elBillAddressCity.getName());
		Assert.assertEquals("${billingAddress}city",elBillAddressCity.getElPlaceholder(false));
		Assert.assertEquals("city",elBillAddressCity.getDbColumn());

//		ElPropertyDeploy elBillAddressCountry = descriptor.getElPropertyDeploy("billingAddress.country");

		
		ElPropertyDeploy elOrders = descriptor.getElPropertyDeploy("orders");
		Assert.assertTrue(elOrders instanceof BeanPropertyAssocMany<?>);
		
		ElPropertyDeploy elOrderStatus = descriptor.getElPropertyDeploy("orders.status");
		Assert.assertTrue(elOrderStatus instanceof ElPropertyChain);
		Assert.assertEquals("orders",elOrderStatus.getElPrefix());
		Assert.assertEquals("status",elOrderStatus.getName());
		Assert.assertEquals("${orders}status",elOrderStatus.getElPlaceholder(false));
		Assert.assertEquals("status",elOrderStatus.getDbColumn());

		ElPropertyDeploy elOrderCust = descriptor.getElPropertyDeploy("orders.customer");
		Assert.assertTrue(elOrderCust instanceof ElPropertyChain);

		ElPropertyDeploy elOrderDetails = descriptor.getElPropertyDeploy("orders.details");
		Assert.assertTrue(elOrderDetails instanceof ElPropertyChain);

		ElPropertyDeploy elOrderDetailsId = descriptor.getElPropertyDeploy("orders.details.id");
		Assert.assertTrue(elOrderDetailsId instanceof ElPropertyChain);
		
	}
	
	
	
}
