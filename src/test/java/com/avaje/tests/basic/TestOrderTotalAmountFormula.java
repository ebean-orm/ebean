package com.avaje.tests.basic;

import java.util.List;

import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestOrderTotalAmountFormula extends TestCase {

    public void testAsJoin() {

    	ResetBasicData.reset();
    	
      List<Customer> l0 = Ebean.find(Customer.class)
    	.select("id, name")
    	.fetch("orders", "status, totalAmount")
    	.where()
    	    .eq("orders.details.product.name", "Desk")
    	    .like("contacts.firstName", "Ji%")

    	.findList();

	    for (Customer c0 : l0) {
			System.out.println("customer: "+c0.getId());
			List<Order> orders = c0.getOrders();
			for (Order order : orders) {
				System.out.println("... order:"+order);
			}
		}

    }
    
}
