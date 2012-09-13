/**
 * Copyright (C) 2009 Authors
 * 
 * This file is part of Ebean.
 * 
 * Ebean is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *  
 * Ebean is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Ebean; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA  
 */
package com.avaje.tests.el;

import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
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
		
		elProp.elGetReference(c0);
		elProp.elGetReference(c1);
        
		addrLine1Prop.elSetValue(c1, "12 someplace", true, false);
		addrCityProp.elSetValue(c1, "Auckland", true, false);
	}
}
