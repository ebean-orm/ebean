package com.avaje.tests.el;

import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;
import com.avaje.tests.model.basic.Address;
import com.avaje.tests.model.basic.Customer;

public class TestElGetReference extends TestCase {

	
	public void test() {
		
		Address a = new Address();
		a.setId((short)12);
		a.setLine1("line1");
		a.setCity("Auckland");
		
		Customer c0 = new Customer();
		c0.setBillingAddress(a);

		Customer c1 = new Customer();

		SpiEbeanServer server = (SpiEbeanServer)Ebean.getServer(null);
		BeanDescriptor<Customer> descriptor = server.getBeanDescriptor(Customer.class);

		ElPropertyValue elProp = descriptor.getElGetValue("billingAddress.id");
		ElPropertyValue addrLine1Prop = descriptor.getElGetValue("billingAddress.line1");
		ElPropertyValue addrCityProp = descriptor.getElGetValue("billingAddress.city");
		
		elProp.elGetReference((EntityBean)c0);
		elProp.elGetReference((EntityBean)c1);
        
		addrLine1Prop.elSetValue((EntityBean)c1, "12 someplace", true);
		addrCityProp.elSetValue((EntityBean)c1, "Auckland", true);
	}
}
