package com.avaje.tests.autofetch;

import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;

import java.util.List;

public class MainAutoQueryTune1 {

	public static void main(String[] args) {

		ResetBasicData.reset();

		MainAutoQueryTune1 me = new MainAutoQueryTune1();		
		me.tuneJoin();
	}

	private void tuneJoin() {
		List<Order> list = Ebean.find(Order.class)
			.setAutoTune(true)
			.fetch("customer")
			.where()
			.eq("status", Order.Status.NEW)
			.eq("customer.name", "Rob")
			.order().asc("id")
			.findList();

		for (Order order : list) {
			order.getId();
      order.getOrderDate();
		}
	}
}
