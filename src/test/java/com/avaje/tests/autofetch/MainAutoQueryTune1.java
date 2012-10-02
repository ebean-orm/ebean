package com.avaje.tests.autofetch;

import java.util.List;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.config.GlobalProperties;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;

public class MainAutoQueryTune1 {

	public static void main(String[] args) {
		
		//GlobalProperties.put("ebean.ddl.run", "false");
		//GlobalProperties.put("ebean.ddl.generate", "false");
		GlobalProperties.put("ebean.autofetch.queryTuning", "true");
//		GlobalProperties.put("ebean.autofetch.queryTuningAddVersion", "true");

		ResetBasicData.reset();

		MainAutoQueryTune1 me = new MainAutoQueryTune1();		
		me.tuneJoin();
	}



	private void tuneJoin()
	{
		List<Order> list = Ebean.find(Order.class)
			.setAutofetch(true)
			.fetch("customer")
			.where()
			.eq("status", Order.Status.NEW)
			.eq("customer.name", "Rob")
			.order().asc("id")
			.findList();

		for (Order order : list)
		{
			System.out.println(order.getId() + " " + order.getOrderDate());
		}
	}
}
