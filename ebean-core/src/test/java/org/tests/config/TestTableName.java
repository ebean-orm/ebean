package org.tests.config;

import io.ebean.BaseTestCase;
import io.ebean.config.TableName;
import org.junit.Assert;
import org.junit.Test;

public class TestTableName extends BaseTestCase {

  @Test
  public void test() {

    TableName t = new TableName("a");
    Assert.assertEquals("a", t.getName());
    Assert.assertNull(t.getCatalog());
    Assert.assertNull(t.getSchema());

    t = new TableName("b.a");
    Assert.assertEquals("a", t.getName());
    Assert.assertEquals("b", t.getSchema());
    Assert.assertNull(t.getCatalog());

    t = new TableName("c.b.a");
    Assert.assertEquals("a", t.getName());
    Assert.assertEquals("b", t.getSchema());
    Assert.assertEquals("c", t.getCatalog());

//		try {
//			TableName t2 = new TableName("d.c.b.a");
//			Assert.assertNotNull(t2);
//			Assert.assertTrue(false);
//		} catch (RuntimeException e){
//			Assert.assertTrue(true);
//		}


//		TableName lhs = new TableName("test.oe_order");
//		TableName rhs = new TableName("test.oe_cust");
//
//		UnderscoreNamingConvention nc = new UnderscoreNamingConvention();
//		TableName intTab = nc.getM2MJoinTableName(lhs, rhs);
//
//		Assert.assertNull(intTab.getCatalog());
//		Assert.assertEquals("test", intTab.getSchema());
//		Assert.assertEquals("oe_order_cust", intTab.getName());
  }
}
