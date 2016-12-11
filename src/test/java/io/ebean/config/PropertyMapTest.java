package io.ebean.config;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PropertyMapTest {

  @Test
  public void testDefaultProperties() throws Exception {

    PropertyMap.defaultProperties();
  }

  @Test
  public void testEval() throws Exception {

    String home = System.getenv("HOME");
    PropertyMap map = new PropertyMap();
    assertEquals(home, map.eval("${HOME}"));
  }

}
