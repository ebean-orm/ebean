package com.avaje.tests.inheritance;


import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.TxRunnable;
import com.avaje.tests.model.basic.AttributeHolder;
import com.avaje.tests.model.basic.ListAttribute;
import com.avaje.tests.model.basic.ListAttributeValue;

public class TestDuplcateKeyException extends BaseTestCase {

	/**
	 * Test query.
	 * <p>This test covers the BUG 276. Cascade was not propagating to the ListAttribute because
	 * it was considered safe to skip as it didn't take into account any derived classes 
	 * into account with e.g. collections and Cascade options </p>
	 */
  @Test
	public void testQuery() {
    
		// Setup the data first
		final ListAttributeValue value1 = new ListAttributeValue();
		
		Ebean.save(value1);
		
		final ListAttribute listAttribute = new ListAttribute();
		listAttribute.add(value1);
		Ebean.save(listAttribute);
		
		
		
		final AttributeHolder holder = new AttributeHolder();
		holder.add(listAttribute);
		
		try {
			Ebean.execute(new TxRunnable() {
				public void run() {
				    //Ebean.currentTransaction().log("-- saving holder first time");
				    // Alternatively turn off cascade Persist for this transaction
				    //Ebean.currentTransaction().setPersistCascade(false);
					Ebean.save(holder);
          //Ebean.currentTransaction().log("-- saving holder second time");
					// we don't get this far before failing
					//Ebean.save(holder);
				}
			});
		} catch (Exception e){
			Assert.assertEquals(e.getMessage(), "test rollback");
		}
	}
}
