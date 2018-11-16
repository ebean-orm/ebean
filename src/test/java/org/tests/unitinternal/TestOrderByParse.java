package org.tests.unitinternal;

import io.ebean.BaseTestCase;
import io.ebean.OrderBy;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test the OrderBy object and especially its parsing.
 */
public class TestOrderByParse extends BaseTestCase {

  @Test
  public void testParsingOne() {

    OrderBy<Object> o1 = new OrderBy<>("id");
    assertTrue(o1.getProperties().size() == 1);
    assertTrue(o1.getProperties().get(0).getProperty().equals("id"));
    assertTrue(o1.getProperties().get(0).isAscending());
    assertTrue(o1.toStringFormat().equals("id"));

    o1 = new OrderBy<>("id asc");
    assertTrue(o1.getProperties().size() == 1);
    assertTrue(o1.getProperties().get(0).getProperty().equals("id"));
    assertTrue(o1.getProperties().get(0).isAscending());
    assertTrue(o1.toStringFormat().equals("id"));

    o1 = new OrderBy<>("id desc");
    assertTrue(o1.getProperties().size() == 1);
    assertTrue(o1.getProperties().get(0).getProperty().equals("id"));
    assertTrue(!o1.getProperties().get(0).isAscending());
    assertTrue(o1.toStringFormat().equals("id desc"));

    o1 = new OrderBy<>(" id  asc ");
    assertTrue(o1.getProperties().size() == 1);
    assertTrue(o1.getProperties().get(0).getProperty().equals("id"));
    assertTrue(o1.getProperties().get(0).isAscending());
    assertTrue(o1.toStringFormat().equals("id"));

    assertTrue(o1.containsProperty("id"));
    assertFalse(o1.containsProperty("junk"));
  }

  @Test
  public void parseNullsHigh() {

    OrderBy<Object> o1 = new OrderBy<>("id desc nulls high");
    assertTrue(o1.getProperties().size() == 1);
    assertTrue(o1.getProperties().get(0).getProperty().equals("id"));
    assertTrue(!o1.getProperties().get(0).isAscending());
    assertTrue(o1.toStringFormat().equals("id desc nulls high"));
  }

  @Test
  public void add_parse() {

    OrderBy<Object> o1 = new OrderBy<>();
    o1.add("id desc nulls high");
    assertTrue(o1.getProperties().size() == 1);
    assertTrue(o1.getProperties().get(0).getProperty().equals("id"));
    assertTrue(!o1.getProperties().get(0).isAscending());
    assertTrue(o1.toStringFormat().equals("id desc nulls high"));
  }

  @Test
  public void parseNullsHigh_with_second() {

    OrderBy<Object> o1 = new OrderBy<>("id desc nulls high, name");
    assertTrue(o1.getProperties().size() == 2);
    assertTrue(o1.getProperties().get(0).getProperty().equals("id"));
    assertTrue(!o1.getProperties().get(0).isAscending());
    assertTrue(o1.toStringFormat().equals("id desc nulls high, name"));
    assertTrue(o1.getProperties().get(1).getProperty().equals("name"));
    assertTrue(o1.getProperties().get(1).isAscending());
  }

  @Test
  public void testParsingTwo() {

    OrderBy<?> o1 = new OrderBy<>("id,name");
    assertTrue(o1.getProperties().size() == 2);
    assertTrue(o1.getProperties().get(0).getProperty().equals("id"));
    assertTrue(o1.getProperties().get(0).isAscending());
    assertTrue(o1.getProperties().get(1).getProperty().equals("name"));
    assertTrue(o1.getProperties().get(1).isAscending());
    assertEquals("id, name", o1.toStringFormat());

    o1 = new OrderBy<>("  id  , name ");
    assertTrue(o1.getProperties().size() == 2);
    assertTrue(o1.getProperties().get(0).getProperty().equals("id"));
    assertTrue(o1.getProperties().get(0).isAscending());
    assertTrue(o1.getProperties().get(1).getProperty().equals("name"));
    assertTrue(o1.getProperties().get(1).isAscending());
    assertEquals("id, name", o1.toStringFormat());

    o1 = new OrderBy<>("  id desc , name  desc ");
    assertTrue(o1.getProperties().size() == 2);
    assertTrue(o1.getProperties().get(0).getProperty().equals("id"));
    assertTrue(!o1.getProperties().get(0).isAscending());
    assertTrue(o1.getProperties().get(1).getProperty().equals("name"));
    assertTrue(!o1.getProperties().get(1).isAscending());
    assertEquals("id desc, name desc", o1.toStringFormat());

    o1 = new OrderBy<>("  id ascending, name  asc");
    assertTrue(o1.getProperties().size() == 2);
    assertTrue(o1.getProperties().get(0).getProperty().equals("id"));
    assertTrue(o1.getProperties().get(0).isAscending());
    assertTrue(o1.getProperties().get(1).getProperty().equals("name"));
    assertTrue(o1.getProperties().get(1).isAscending());
    assertEquals("id, name", o1.toStringFormat());

  }

  @Test
  public void testAddMethods() {

    OrderBy<?> o1 = new OrderBy<>();
    o1.asc("id");
    o1.asc("name");
    assertTrue(o1.getProperties().size() == 2);
    assertTrue(o1.getProperties().get(0).getProperty().equals("id"));
    assertTrue(o1.getProperties().get(0).isAscending());
    assertTrue(o1.getProperties().get(1).getProperty().equals("name"));
    assertTrue(o1.getProperties().get(1).isAscending());
    assertEquals("id, name", o1.toStringFormat());

    o1 = new OrderBy<>();
    o1.desc("id");
    o1.desc("name");
    assertTrue(o1.getProperties().size() == 2);
    assertTrue(o1.getProperties().get(0).getProperty().equals("id"));
    assertTrue(!o1.getProperties().get(0).isAscending());
    assertTrue(o1.getProperties().get(1).getProperty().equals("name"));
    assertTrue(!o1.getProperties().get(1).isAscending());
    assertEquals("id desc, name desc", o1.toStringFormat());

    o1.reverse();
    assertTrue(o1.getProperties().size() == 2);
    assertTrue(o1.getProperties().get(0).getProperty().equals("id"));
    assertTrue(o1.getProperties().get(0).isAscending());
    assertTrue(o1.getProperties().get(1).getProperty().equals("name"));
    assertTrue(o1.getProperties().get(1).isAscending());
    assertEquals("id, name", o1.toStringFormat());

    OrderBy<?> copy = o1.copy();
    assertTrue(copy != o1);
    assertTrue(copy.getProperties().size() == 2);
    assertTrue(copy.getProperties().get(0).getProperty().equals("id"));
    assertTrue(copy.getProperties().get(0).isAscending());
    assertTrue(copy.getProperties().get(1).getProperty().equals("name"));
    assertTrue(copy.getProperties().get(1).isAscending());
    assertEquals("id, name", copy.toStringFormat());

  }

  @Test
  public void testParsingWithCollation() {

    OrderBy<Object> o1 = new OrderBy<>();
    o1.asc("id", "latin_1");
    assertTrue(o1.getProperties().size() == 1);
    assertTrue(o1.getProperties().get(0).getProperty().equals("id"));
    assertTrue(o1.getProperties().get(0).isAscending());
    assertEquals("id collate latin_1", o1.toStringFormat());

    o1 = new OrderBy<>();
    o1.desc("id", "latin_1");
    assertTrue(o1.getProperties().size() == 1);
    assertTrue(o1.getProperties().get(0).getProperty().equals("id"));
    assertTrue(!o1.getProperties().get(0).isAscending());
    assertEquals("id collate latin_1 desc", o1.toStringFormat());

    o1 = new OrderBy<>();
    o1.desc("id", "latin_1");
    o1.asc("date");
    assertTrue(o1.getProperties().size() == 2);
    assertTrue(o1.getProperties().get(0).getProperty().equals("id"));
    assertTrue(o1.getProperties().get(1).getProperty().equals("date"));
    assertTrue(!o1.getProperties().get(0).isAscending());
    assertTrue(o1.getProperties().get(1).isAscending());
    assertEquals("id collate latin_1 desc, date", o1.toStringFormat());

    o1 = new OrderBy<>();
    o1.desc("id", "latin_1");
    o1.asc("name", "latin_2");
    assertTrue(o1.getProperties().size() == 2);
    assertTrue(o1.getProperties().get(0).getProperty().equals("id"));
    assertTrue(o1.getProperties().get(1).getProperty().equals("name"));
    assertTrue(!o1.getProperties().get(0).isAscending());
    assertTrue(o1.getProperties().get(1).isAscending());
    assertEquals("id collate latin_1 desc, name collate latin_2", o1.toStringFormat());

    // functional (DB2) syntax
    o1 = new OrderBy<>();
    o1.desc("id", "COLLATION_KEY(${}, 'latin_1')");
    assertTrue(o1.getProperties().size() == 1);
    assertTrue(o1.getProperties().get(0).getProperty().equals("id"));
    assertTrue(!o1.getProperties().get(0).isAscending());
    assertEquals("COLLATION_KEY(id, 'latin_1') desc", o1.toStringFormat());

  }

  @Test
  public void equals_with_nulls() {

    OrderBy<Object> o1 = new OrderBy<>("id desc nulls high");
    OrderBy<Object> o2 = new OrderBy<>("id desc nulls high");
    OrderBy<Object> o3 = new OrderBy<>();
    o3.add("id desc nulls high");

    assertEquals(o1, o2);
    assertEquals(o1, o3);


    OrderBy<Object> o4 = new OrderBy<>("id desc");
    OrderBy<Object> o5 = new OrderBy<>("oid desc nulls high");
    OrderBy<Object> o6 = new OrderBy<>("id desc nulls low");

    assertNotEquals(o1, o4);
    assertNotEquals(o1, o5);
    assertNotEquals(o1, o6);
  }

  @Test
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
