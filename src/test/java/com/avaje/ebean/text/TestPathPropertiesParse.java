package com.avaje.ebean.text;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;

public class TestPathPropertiesParse extends BaseTestCase {

  @Test
  public void test() {

    PathProperties s0 = PathProperties.parse("(id,name)");

    Assert.assertEquals(1, s0.getPaths().size());
    Assert.assertTrue(s0.get(null).contains("id"));
    Assert.assertTrue(s0.get(null).contains("name"));
    Assert.assertFalse(s0.get(null).contains("status"));

    PathProperties s1 = PathProperties.parse(":(id,name,shipAddr(*))");
    Assert.assertEquals(2, s1.getPaths().size());
    Assert.assertEquals(3, s1.get(null).size());
    Assert.assertTrue(s1.get(null).contains("id"));
    Assert.assertTrue(s1.get(null).contains("name"));
    Assert.assertTrue(s1.get(null).contains("shipAddr"));
    Assert.assertTrue(s1.get("shipAddr").contains("*"));
    Assert.assertEquals(1, s1.get("shipAddr").size());

  }

}
