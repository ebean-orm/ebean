package com.avaje.tests.basic;

import junit.framework.Assert;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.PersistentFile;
import com.avaje.tests.model.basic.PersistentFileContent;

public class TestSaveDeleteOneToOne extends BaseTestCase {

  @Test
	public void testCreateDeletePersistentFile() {
		PersistentFile persistentFile = new PersistentFile("test.txt",
				new PersistentFileContent("test".getBytes()));

		Ebean.save(persistentFile);
		Ebean.delete(persistentFile);
	}

  @Test
	public void testCreateLoadDeletePersistentFile() {
		PersistentFile persistentFile = new PersistentFile("test.txt",
				new PersistentFileContent("test".getBytes()));

		Ebean.save(persistentFile);

		persistentFile = Ebean.find(PersistentFile.class, persistentFile.getId());
		
		PersistentFileContent persistentFileContent = persistentFile.getPersistentFileContent();
		
		Assert.assertNotNull(persistentFileContent);
		
		Assert.assertNotNull("load byte content", persistentFileContent.getContent());
		
		Ebean.delete(persistentFile);
	}
}
