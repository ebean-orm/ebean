package com.avaje.tests.basic.event;

import junit.framework.Assert;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.TWithPreInsert;

public class TestPreInsertValidation extends BaseTestCase {

  @Test
	public void test() {
		
		TWithPreInsert e = new TWithPreInsert();
		e.setTitle("Mister");
		// the perInsert should populate the
		// name with should not be null
		Ebean.save(e);
		
		// the save worked
		Assert.assertNotNull(e.getId());
		
		TWithPreInsert e1 = Ebean.find(TWithPreInsert.class, e.getId());
		
		e1.setTitle("Missus");
		Ebean.save(e1);
	}
	
}
