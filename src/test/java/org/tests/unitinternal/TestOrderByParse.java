package org.tests.unitinternal;

import io.ebean.BaseTestCase;
import io.ebean.OrderBy;
import org.junit.Test;

import static org.junit.Assert.*;

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
}
