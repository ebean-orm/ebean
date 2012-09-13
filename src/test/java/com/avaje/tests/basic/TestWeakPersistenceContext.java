

package com.avaje.tests.basic;

import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestWeakPersistenceContext extends TestCase {

	
	public void testOne() {
		
		PersistenceContext ctx = inner();
		
		System.gc();
		
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// this is really only a HINT, so no guarantee
		// .. but the SUN JVM does do the business 
		System.gc();

		// Pass on the SUN JVM 
		Object o3 = ctx.get(Order.class, 1);
		Assert.assertNull("Sun JVM should have GC'ed this bean",o3);
		
	}
	
	private PersistenceContext inner() {
		
		ResetBasicData.reset();
		
		Transaction transaction = Ebean.beginTransaction();
		SpiTransaction st = (SpiTransaction)transaction;
		PersistenceContext ctx = st.getPersistenceContext();
		
		List<Order> list = Ebean.find(Order.class)
			//.select("id")
			.findList();

		Assert.assertTrue(list.size() > 0);
		
		Object o1 = ctx.get(Order.class, 1);

		Assert.assertNotNull(o1);
		
		Ebean.endTransaction();
		
		Object o2 = ctx.get(Order.class, 1);
		Assert.assertNotNull(o2);
		
		System.gc();

		return ctx;
	}
}
