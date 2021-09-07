package org.tests.idkeys;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.ESimple;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestSimpleIdInsert extends BaseTestCase {

  @Test
  public void test() {
    ESimple e = new ESimple();
    e.setName("name");

    DB.save(e);

    assertNotNull(e.getId());

  }

//	// This test fails with jdbc drivers that don't
//	// support batch insert with getGeneratedKeys
//	public void testJdbcBatch() {
//
//		GlobalProperties.put("datasource.default", "hsqldb");
//		GlobalProperties.put("ebean.classes", ESimple.class.getName());
//
//		Transaction transaction = DB.beginTransaction();
//		try {
//			transaction.setBatchMode(true);
//			transaction.setLogLevel(LogLevel.SQL);
//			ESimple e = new ESimple();
//			e.setName("name");
//			DB.save(e);
//
//			ESimple e2 = new ESimple();
//			e2.setName("name2");
//			DB.save(e2);
//			transaction.commit();
//
//			Assert.assertNotNull(e.getId());
//			Assert.assertNotNull(e2.getId());
//
//		} finally {
//			DB.endTransaction();
//		}
//	}

}
