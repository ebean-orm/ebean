package com.avaje.ebean.config;

import org.junit.Test;

import static org.junit.Assert.*;

public class PropertyMapTest {

  @Test
  public void testDefaultProperties() throws Exception {

  }


  @Test
  public void testAsProperties() throws Exception {

  }

  @Test
  public void testEvaluateProperties() throws Exception {

  }

  @Test
  public void testEval() throws Exception {

    String home = System.getenv("HOME");
    PropertyMap map = new PropertyMap();
    assertEquals(home, map.eval("${HOME}"));


  }

  @Test
  public void testGetBoolean() throws Exception {

  }

  @Test
  public void testGetInt() throws Exception {

  }

  @Test
  public void testGetLong() throws Exception {

  }

  @Test
  public void testGet() throws Exception {

  }

  @Test
  public void testGet1() throws Exception {

  }

  @Test
  public void testPutEvalAll() throws Exception {

  }

  @Test
  public void testPutEval() throws Exception {

  }

  @Test
  public void testPut() throws Exception {

  }

  @Test
  public void testRemove() throws Exception {

  }

  @Test
  public void testEntrySet() throws Exception {

  }
}