package com.avaje.tests.idkeys;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
//import com.avaje.ebean.LogLevel;
//import com.avaje.ebean.Transaction;
import com.avaje.ebean.config.GlobalProperties;
import com.avaje.tests.model.basic.ESimple;

public class TestSimpleIdInsert extends TestCase {

	public void test() {

		GlobalProperties.put("datasource.default", "h2");
		GlobalProperties.put("ebean.classes", ESimple.class.getName());

		ESimple e = new ESimple();
		e.setName("name");

		Ebean.save(e);

		Assert.assertNotNull(e.getId());

	}

//	// This test fails with jdbc drivers that don't
//	// support batch insert with getGeneratedKeys
//	public void testJdbcBatch() {
//
//		GlobalProperties.put("datasource.default", "hsqldb");
//		GlobalProperties.put("ebean.classes", ESimple.class.getName());
//
//		Transaction transaction = Ebean.beginTransaction();
//		try {
//			transaction.setBatchMode(true);
//			transaction.setLogLevel(LogLevel.SQL);
//			ESimple e = new ESimple();
//			e.setName("name");
//			Ebean.save(e);
//
//			ESimple e2 = new ESimple();
//			e2.setName("name2");
//			Ebean.save(e2);
//			transaction.commit();
//
//			Assert.assertNotNull(e.getId());
//			Assert.assertNotNull(e2.getId());
//
//		} finally {
//			Ebean.endTransaction();
//		}
//	}

}
