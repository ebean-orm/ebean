package io.ebean.xtest.config;

import io.ebean.BaseTestCase;
import io.ebean.config.TableName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestTableName extends BaseTestCase {

  @Test
  public void withCatalogAndSchema() {
    TableName t = new TableName("a.b.c");
    assertThat(t.withCatalogAndSchema("foo")).isEqualTo("a.b.foo");
  }

  @Test
  public void withCatalogAndSchema_when_quoted() {
    TableName t = new TableName("[a].[b].[c]");
    assertThat(t.withCatalogAndSchema("foo")).isEqualTo("[a].[b].foo");

    TableName noCat = new TableName("[b].[c]");
    assertThat(noCat.withCatalogAndSchema("foo")).isEqualTo("[b].foo");

    TableName tabOnly = new TableName("[c]");
    assertThat(tabOnly.withCatalogAndSchema("foo")).isEqualTo("foo");
  }

  @Test
  public void test() {

    TableName t = new TableName("a");
    assertEquals("a", t.getName());
    assertNull(t.getCatalog());
    assertNull(t.getSchema());

    t = new TableName("b.a");
    assertEquals("a", t.getName());
    assertEquals("b", t.getSchema());
    assertNull(t.getCatalog());

    t = new TableName("c.b.a");
    assertEquals("a", t.getName());
    assertEquals("b", t.getSchema());
    assertEquals("c", t.getCatalog());

//		try {
//			TableName t2 = new TableName("d.c.b.a");
//			assertNotNull(t2);
//			assertTrue(false);
//		} catch (RuntimeException e){
//			assertTrue(true);
//		}


//		TableName lhs = new TableName("test.oe_order");
//		TableName rhs = new TableName("test.oe_cust");
//
//		UnderscoreNamingConvention nc = new UnderscoreNamingConvention();
//		TableName intTab = nc.getM2MJoinTableName(lhs, rhs);
//
//		assertNull(intTab.getCatalog());
//		assertEquals("test", intTab.getSchema());
//		assertEquals("oe_order_cust", intTab.getName());
  }
}
