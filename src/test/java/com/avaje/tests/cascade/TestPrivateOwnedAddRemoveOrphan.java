package com.avaje.tests.cascade;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.TSDetail;
import com.avaje.tests.model.basic.TSMaster;

public class TestPrivateOwnedAddRemoveOrphan extends BaseTestCase {
	
	@Test
	public void test(){
		
	  // setup
		TSMaster master0 = new TSMaster();
		Ebean.save(master0);
		
		// act
		TSMaster master1 = Ebean.find(master0.getClass(), master0.getId());
		
		TSDetail tsDetail = new TSDetail();
		// Add then remove a bean that was never saved (to the DB)
		master1.getDetails().add(tsDetail);
		master1.getDetails().remove(tsDetail);

		Ebean.save(master1);
		
		TSMaster master2 = Ebean.find(master1.getClass(), master1.getId());
		
		Assert.assertTrue(master2.getDetails().isEmpty());
	}
}
