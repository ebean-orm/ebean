package com.avaje.tests.unitinternal;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.OrderBy;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test the OrderBy object and especially its parsing.
 */
public class TestOrderByParse extends BaseTestCase {

  @Test
  public void testParsingOne() {

    OrderBy<Object> o1 = new OrderBy<Object>("id");
    assertTrue(o1.getProperties().size() == 1);
    assertTrue(o1.getProperties().get(0).getProperty().equals("id"));
    assertTrue(o1.getProperties().get(0).isAscending());
    assertTrue(o1.toStringFormat().equals("id"));

    o1 = new OrderBy<Object>("id asc");
    assertTrue(o1.getProperties().size() == 1);
    assertTrue(o1.getProperties().get(0).getProperty().equals("id"));
    assertTrue(o1.getProperties().get(0).isAscending());
    assertTrue(o1.toStringFormat().equals("id"));

    o1 = new OrderBy<Object>("id desc");
    assertTrue(o1.getProperties().size() == 1);
    assertTrue(o1.getProperties().get(0).getProperty().equals("id"));
    assertTrue(!o1.getProperties().get(0).isAscending());
    assertTrue(o1.toStringFormat().equals("id desc"));

    o1 = new OrderBy<Object>(" id  asc ");
    assertTrue(o1.getProperties().size() == 1);
    assertTrue(o1.getProperties().get(0).getProperty().equals("id"));
    assertTrue(o1.getProperties().get(0).isAscending());
    assertTrue(o1.toStringFormat().equals("id"));

    assertTrue(o1.containsProperty("id"));
    assertFalse(o1.containsProperty("junk"));
  }

  @Test
  public void testParsingTwo() {

    OrderBy<?> o1 = new OrderBy<Object>("id,name");
    assertTrue(o1.getProperties().size() == 2);
    assertTrue(o1.getProperties().get(0).getProperty().equals("id"));
    assertTrue(o1.getProperties().get(0).isAscending());
    assertTrue(o1.getProperties().get(1).getProperty().equals("name"));
    assertTrue(o1.getProperties().get(1).isAscending());
    assertEquals("id, name", o1.toStringFormat());

    o1 = new OrderBy<Object>("  id  , name ");
    assertTrue(o1.getProperties().size() == 2);
    assertTrue(o1.getProperties().get(0).getProperty().equals("id"));
    assertTrue(o1.getProperties().get(0).isAscending());
    assertTrue(o1.getProperties().get(1).getProperty().equals("name"));
    assertTrue(o1.getProperties().get(1).isAscending());
    assertEquals("id, name", o1.toStringFormat());

    o1 = new OrderBy<Object>("  id desc , name  desc ");
    assertTrue(o1.getProperties().size() == 2);
    assertTrue(o1.getProperties().get(0).getProperty().equals("id"));
    assertTrue(!o1.getProperties().get(0).isAscending());
    assertTrue(o1.getProperties().get(1).getProperty().equals("name"));
    assertTrue(!o1.getProperties().get(1).isAscending());
    assertEquals("id desc, name desc", o1.toStringFormat());

    o1 = new OrderBy<Object>("  id ascending, name  asc");
    assertTrue(o1.getProperties().size() == 2);
    assertTrue(o1.getProperties().get(0).getProperty().equals("id"));
    assertTrue(o1.getProperties().get(0).isAscending());
    assertTrue(o1.getProperties().get(1).getProperty().equals("name"));
    assertTrue(o1.getProperties().get(1).isAscending());
    assertEquals("id, name", o1.toStringFormat());

  }

  @Test
  public void testAddMethods() {

    OrderBy<?> o1 = new OrderBy<Object>();
    o1.asc("id");
    o1.asc("name");
    assertTrue(o1.getProperties().size() == 2);
    assertTrue(o1.getProperties().get(0).getProperty().equals("id"));
    assertTrue(o1.getProperties().get(0).isAscending());
    assertTrue(o1.getProperties().get(1).getProperty().equals("name"));
    assertTrue(o1.getProperties().get(1).isAscending());
    assertEquals("id, name", o1.toStringFormat());

    o1 = new OrderBy<Object>();
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
