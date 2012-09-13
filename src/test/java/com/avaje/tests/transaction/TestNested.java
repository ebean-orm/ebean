package com.avaje.tests.transaction;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.TxRunnable;

public class TestNested extends TestCase {

	
	public void test() {
		
		try {
			Ebean.execute(new TxRunnable() {
				public void run() {
					
					willFail();
				}
			});
		} catch (RuntimeException e){
			Assert.assertEquals(e.getMessage(), "test rollback");
		}
	}
	
	private void willFail() {
		Ebean.execute(new TxRunnable() {
			public void run() {
				
				String msg = "test rollback";
				throw new RuntimeException(msg);

			}
		});
	}
}
