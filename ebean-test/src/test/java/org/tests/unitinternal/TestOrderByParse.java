package org.tests.unitinternal;

import io.ebean.xtest.BaseTestCase;
import io.ebean.OrderBy;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the OrderBy object and especially its parsing.
 */
public class TestOrderByParse extends BaseTestCase {

  @Test
  public void testParseRaw() {

    OrderBy<Object> o1 = OrderBy.of("case when status='N' then 1 when status='F' then 2 else 99 end");
    assertEquals(1, o1.getProperties().size());
    assertTrue(o1.getProperties().get(0).isAscending());
    assertThat(o1.toStringFormat()).isEqualTo("case when status='N' then 1 when status='F' then 2 else 99 end");
    assertThat(o1.getProperties().get(0).getProperty()).isEqualTo("case when status='N' then 1 when status='F' then 2 else 99 end");
  }

  @Test
  public void testParsingOne() {

    OrderBy<Object> o1 = OrderBy.of("id");
    assertEquals(1, o1.getProperties().size());
    assertEquals("id", o1.getProperties().get(0).getProperty());
    assertTrue(o1.getProperties().get(0).isAscending());
    assertEquals("id", o1.toStringFormat());

    o1 = OrderBy.of("id asc");
    assertEquals(1, o1.getProperties().size());
    assertEquals("id", o1.getProperties().get(0).getProperty());
    assertTrue(o1.getProperties().get(0).isAscending());
    assertEquals("id", o1.toStringFormat());

    o1 = OrderBy.of("id desc");
    assertEquals(1, o1.getProperties().size());
    assertEquals("id", o1.getProperties().get(0).getProperty());
    assertFalse(o1.getProperties().get(0).isAscending());
    assertEquals("id desc", o1.toStringFormat());

    o1 = OrderBy.of(" id  asc ");
    assertEquals(1, o1.getProperties().size());
    assertEquals("id", o1.getProperties().get(0).getProperty());
    assertTrue(o1.getProperties().get(0).isAscending());
    assertEquals("id", o1.toStringFormat());

    assertTrue(o1.containsProperty("id"));
    assertFalse(o1.containsProperty("junk"));
  }

  @Test
  public void parseNullsHigh() {

    OrderBy<Object> o1 = OrderBy.of("id desc nulls high");
    assertEquals(1, o1.getProperties().size());
    assertEquals("id", o1.getProperties().get(0).getProperty());
    assertFalse(o1.getProperties().get(0).isAscending());
    assertEquals("id desc nulls high", o1.toStringFormat());
  }

  @Test
  @SuppressWarnings("removal") // uses internal API
  public void add_parse() {

    OrderBy<Object> o1 = new OrderBy<>();
    o1.add("id desc nulls high");
    assertEquals(1, o1.getProperties().size());
    assertEquals("id", o1.getProperties().get(0).getProperty());
    assertFalse(o1.getProperties().get(0).isAscending());
    assertEquals("id desc nulls high", o1.toStringFormat());
  }

  @Test
  public void parseNullsHigh_with_second() {

    OrderBy<Object> o1 = OrderBy.of("id desc nulls high, name");
    assertEquals(2, o1.getProperties().size());
    assertEquals("id", o1.getProperties().get(0).getProperty());
    assertFalse(o1.getProperties().get(0).isAscending());
    assertEquals("id desc nulls high, name", o1.toStringFormat());
    assertEquals("name", o1.getProperties().get(1).getProperty());
    assertTrue(o1.getProperties().get(1).isAscending());
  }

  @Test
  public void testParsingTwo() {

    OrderBy<?> o1 = OrderBy.of("id,name");
    assertEquals(2, o1.getProperties().size());
    assertEquals("id", o1.getProperties().get(0).getProperty());
    assertTrue(o1.getProperties().get(0).isAscending());
    assertEquals("name", o1.getProperties().get(1).getProperty());
    assertTrue(o1.getProperties().get(1).isAscending());
    assertEquals("id, name", o1.toStringFormat());

    o1 = OrderBy.of("  id  , name ");
    assertEquals(2, o1.getProperties().size());
    assertEquals("id", o1.getProperties().get(0).getProperty());
    assertTrue(o1.getProperties().get(0).isAscending());
    assertEquals("name", o1.getProperties().get(1).getProperty());
    assertTrue(o1.getProperties().get(1).isAscending());
    assertEquals("id, name", o1.toStringFormat());

    o1 = OrderBy.of("  id desc , name  desc ");
    assertEquals(2, o1.getProperties().size());
    assertEquals("id", o1.getProperties().get(0).getProperty());
    assertFalse(o1.getProperties().get(0).isAscending());
    assertEquals("name", o1.getProperties().get(1).getProperty());
    assertFalse(o1.getProperties().get(1).isAscending());
    assertEquals("id desc, name desc", o1.toStringFormat());

    o1 = OrderBy.of("  id ascending, name  asc");
    assertEquals(2, o1.getProperties().size());
    assertEquals("id", o1.getProperties().get(0).getProperty());
    assertTrue(o1.getProperties().get(0).isAscending());
    assertEquals("name", o1.getProperties().get(1).getProperty());
    assertTrue(o1.getProperties().get(1).isAscending());
    assertEquals("id, name", o1.toStringFormat());

  }

  @Test
  @SuppressWarnings("removal") // uses internal API
  public void testAddMethods() {

    OrderBy<?> o1 = new OrderBy<>();
    o1.asc("id");
    o1.asc("name");
    assertEquals(2, o1.getProperties().size());
    assertEquals("id", o1.getProperties().get(0).getProperty());
    assertTrue(o1.getProperties().get(0).isAscending());
    assertEquals("name", o1.getProperties().get(1).getProperty());
    assertTrue(o1.getProperties().get(1).isAscending());
    assertEquals("id, name", o1.toStringFormat());

    o1 = new OrderBy<>();
    o1.desc("id");
    o1.desc("name");
    assertEquals(2, o1.getProperties().size());
    assertEquals("id", o1.getProperties().get(0).getProperty());
    assertFalse(o1.getProperties().get(0).isAscending());
    assertEquals("name", o1.getProperties().get(1).getProperty());
    assertFalse(o1.getProperties().get(1).isAscending());
    assertEquals("id desc, name desc", o1.toStringFormat());

    o1.reverse();
    assertEquals(2, o1.getProperties().size());
    assertEquals("id", o1.getProperties().get(0).getProperty());
    assertTrue(o1.getProperties().get(0).isAscending());
    assertEquals("name", o1.getProperties().get(1).getProperty());
    assertTrue(o1.getProperties().get(1).isAscending());
    assertEquals("id, name", o1.toStringFormat());

    OrderBy<?> copy = o1.copy();
    assertNotSame(copy, o1);
    assertEquals(2, copy.getProperties().size());
    assertEquals("id", copy.getProperties().get(0).getProperty());
    assertTrue(copy.getProperties().get(0).isAscending());
    assertEquals("name", copy.getProperties().get(1).getProperty());
    assertTrue(copy.getProperties().get(1).isAscending());
    assertEquals("id, name", copy.toStringFormat());

  }

  @Test
  @SuppressWarnings("removal") // uses internal API
  public void testParsingWithCollation() {

    OrderBy<Object> o1 = new OrderBy<>();
    o1.asc("id", "latin_1");
    assertEquals(1, o1.getProperties().size());
    assertEquals("id", o1.getProperties().get(0).getProperty());
    assertTrue(o1.getProperties().get(0).isAscending());
    assertEquals("id collate latin_1", o1.toStringFormat());

    o1 = new OrderBy<>();
    o1.desc("id", "latin_1");
    assertEquals(1, o1.getProperties().size());
    assertEquals("id", o1.getProperties().get(0).getProperty());
    assertFalse(o1.getProperties().get(0).isAscending());
    assertEquals("id collate latin_1 desc", o1.toStringFormat());

    o1 = new OrderBy<>();
    o1.desc("id", "latin_1");
    o1.asc("date");
    assertEquals(2, o1.getProperties().size());
    assertEquals("id", o1.getProperties().get(0).getProperty());
    assertEquals("date", o1.getProperties().get(1).getProperty());
    assertFalse(o1.getProperties().get(0).isAscending());
    assertTrue(o1.getProperties().get(1).isAscending());
    assertEquals("id collate latin_1 desc, date", o1.toStringFormat());

    o1 = new OrderBy<>();
    o1.desc("id", "latin_1");
    o1.asc("name", "latin_2");
    assertEquals(2, o1.getProperties().size());
    assertEquals("id", o1.getProperties().get(0).getProperty());
    assertEquals("name", o1.getProperties().get(1).getProperty());
    assertFalse(o1.getProperties().get(0).isAscending());
    assertTrue(o1.getProperties().get(1).isAscending());
    assertEquals("id collate latin_1 desc, name collate latin_2", o1.toStringFormat());

    // functional (DB2) syntax
    o1 = new OrderBy<>();
    o1.desc("id", "COLLATION_KEY(${}, 'latin_1')");
    assertEquals(1, o1.getProperties().size());
    assertEquals("id", o1.getProperties().get(0).getProperty());
    assertFalse(o1.getProperties().get(0).isAscending());
    assertEquals("COLLATION_KEY(id, 'latin_1') desc", o1.toStringFormat());
  }

  @Test
  @SuppressWarnings("removal") // uses internal API
  public void equals_with_nulls() {

    OrderBy<Object> o1 = OrderBy.of("id desc nulls high");
    OrderBy<Object> o2 = OrderBy.of("id desc nulls high");
    OrderBy<Object> o3 = new OrderBy<>();
    o3.add("id desc nulls high");

    assertEquals(o1, o2);
    assertEquals(o1, o3);

    OrderBy<Object> o4 = OrderBy.of("id desc");
    OrderBy<Object> o5 = OrderBy.of("oid desc nulls high");
    OrderBy<Object> o6 = OrderBy.of("id desc nulls low");

    assertNotEquals(o1, o4);
    assertNotEquals(o1, o5);
    assertNotEquals(o1, o6);
  }

  @Test
  @SuppressWarnings("removal") // uses internal API
  public void equals_with_collation() {
    OrderBy<Object> o1 = new OrderBy<>();
    o1.asc("name", "latin_1");

    OrderBy<Object> o2 = new OrderBy<>();
    o2.asc("name", null);

    OrderBy<Object> o3 = new OrderBy<>();
    o2.asc("name", "bar");

    assertNotEquals(o1, o2);
    assertNotEquals(o1, o3);

    OrderBy<Object> o4 = new OrderBy<>();
    o4.asc("name", "latin_1");
    assertEquals(o1, o4);
  }
}
