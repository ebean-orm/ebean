package org.tests.idkeys;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.ESimple;
import org.junit.Assert;
import org.junit.Test;

public class TestSimpleIdInsert extends BaseTestCase {

  @Test
  public void test() {

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
