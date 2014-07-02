package com.avaje.tests.unitinternal;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.OrderBy;

/**
 * Test the OrderBy object and especially its parsing.
 */
public class TestOrderByParse extends BaseTestCase {

  @Test
  public void testParsingOne() {

    OrderBy<Object> o1 = new OrderBy<Object>("id");
    Assert.assertTrue(o1.getProperties().size() == 1);
    Assert.assertTrue(o1.getProperties().get(0).getProperty().equals("id"));
    Assert.assertTrue(o1.getProperties().get(0).isAscending());
    Assert.assertTrue(o1.toStringFormat().equals("id"));

    o1 = new OrderBy<Object>("id asc");
    Assert.assertTrue(o1.getProperties().size() == 1);
    Assert.assertTrue(o1.getProperties().get(0).getProperty().equals("id"));
    Assert.assertTrue(o1.getProperties().get(0).isAscending());
    Assert.assertTrue(o1.toStringFormat().equals("id"));

    o1 = new OrderBy<Object>("id desc");
    Assert.assertTrue(o1.getProperties().size() == 1);
    Assert.assertTrue(o1.getProperties().get(0).getProperty().equals("id"));
    Assert.assertTrue(!o1.getProperties().get(0).isAscending());
    Assert.assertTrue(o1.toStringFormat().equals("id desc"));

    o1 = new OrderBy<Object>(" id  asc ");
    Assert.assertTrue(o1.getProperties().size() == 1);
    Assert.assertTrue(o1.getProperties().get(0).getProperty().equals("id"));
    Assert.assertTrue(o1.getProperties().get(0).isAscending());
    Assert.assertTrue(o1.toStringFormat().equals("id"));

  }

  public void testParsingTwo() {

    OrderBy<?> o1 = new OrderBy<Object>("id,name");
    Assert.assertTrue(o1.getProperties().size() == 2);
    Assert.assertTrue(o1.getProperties().get(0).getProperty().equals("id"));
    Assert.assertTrue(o1.getProperties().get(0).isAscending());
    Assert.assertTrue(o1.getProperties().get(1).getProperty().equals("name"));
    Assert.assertTrue(o1.getProperties().get(1).isAscending());
    Assert.assertEquals("id, name", o1.toStringFormat());

    o1 = new OrderBy<Object>("  id  , name ");
    Assert.assertTrue(o1.getProperties().size() == 2);
    Assert.assertTrue(o1.getProperties().get(0).getProperty().equals("id"));
    Assert.assertTrue(o1.getProperties().get(0).isAscending());
    Assert.assertTrue(o1.getProperties().get(1).getProperty().equals("name"));
    Assert.assertTrue(o1.getProperties().get(1).isAscending());
    Assert.assertEquals("id, name", o1.toStringFormat());

    o1 = new OrderBy<Object>("  id desc , name  desc ");
    Assert.assertTrue(o1.getProperties().size() == 2);
    Assert.assertTrue(o1.getProperties().get(0).getProperty().equals("id"));
    Assert.assertTrue(!o1.getProperties().get(0).isAscending());
    Assert.assertTrue(o1.getProperties().get(1).getProperty().equals("name"));
    Assert.assertTrue(!o1.getProperties().get(1).isAscending());
    Assert.assertEquals("id desc, name desc", o1.toStringFormat());

    o1 = new OrderBy<Object>("  id ascending, name  asc");
    Assert.assertTrue(o1.getProperties().size() == 2);
    Assert.assertTrue(o1.getProperties().get(0).getProperty().equals("id"));
    Assert.assertTrue(o1.getProperties().get(0).isAscending());
    Assert.assertTrue(o1.getProperties().get(1).getProperty().equals("name"));
    Assert.assertTrue(o1.getProperties().get(1).isAscending());
    Assert.assertEquals("id, name", o1.toStringFormat());

  }

  public void testAddMethods() {

    OrderBy<?> o1 = new OrderBy<Object>();
    o1.asc("id");
    o1.asc("name");
    Assert.assertTrue(o1.getProperties().size() == 2);
    Assert.assertTrue(o1.getProperties().get(0).getProperty().equals("id"));
    Assert.assertTrue(o1.getProperties().get(0).isAscending());
    Assert.assertTrue(o1.getProperties().get(1).getProperty().equals("name"));
    Assert.assertTrue(o1.getProperties().get(1).isAscending());
    Assert.assertEquals("id, name", o1.toStringFormat());

    o1 = new OrderBy<Object>();
    o1.desc("id");
    o1.desc("name");
    Assert.assertTrue(o1.getProperties().size() == 2);
    Assert.assertTrue(o1.getProperties().get(0).getProperty().equals("id"));
    Assert.assertTrue(!o1.getProperties().get(0).isAscending());
    Assert.assertTrue(o1.getProperties().get(1).getProperty().equals("name"));
    Assert.assertTrue(!o1.getProperties().get(1).isAscending());
    Assert.assertEquals("id desc, name desc", o1.toStringFormat());

    o1.reverse();
    Assert.assertTrue(o1.getProperties().size() == 2);
    Assert.assertTrue(o1.getProperties().get(0).getProperty().equals("id"));
    Assert.assertTrue(o1.getProperties().get(0).isAscending());
    Assert.assertTrue(o1.getProperties().get(1).getProperty().equals("name"));
    Assert.assertTrue(o1.getProperties().get(1).isAscending());
    Assert.assertEquals("id, name", o1.toStringFormat());

    OrderBy<?> copy = o1.copy();
    Assert.assertTrue(copy != o1);
    Assert.assertTrue(copy.getProperties().size() == 2);
    Assert.assertTrue(copy.getProperties().get(0).getProperty().equals("id"));
    Assert.assertTrue(copy.getProperties().get(0).isAscending());
    Assert.assertTrue(copy.getProperties().get(1).getProperty().equals("name"));
    Assert.assertTrue(copy.getProperties().get(1).isAscending());
    Assert.assertEquals("id, name", copy.toStringFormat());

  }
}
