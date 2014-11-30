package com.avaje.ebean;

import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

public class PrimaryServerTest {


  @Test
  public void testIsSkipPrimaryServer() throws Exception {
    PrimaryServer.setSkip(true);
    assertTrue(PrimaryServer.isSkip());
    PrimaryServer.setSkip(false);
    assertFalse(PrimaryServer.isSkip());
  }

  @Test
  public void testGetPrimaryServerName() throws Exception {

    String primaryServerName = PrimaryServer.getPrimaryServerName();
    assertEquals("h2", primaryServerName);
  }

  @Test
  public void testLoadProperties() throws Exception {

    Properties properties = PrimaryServer.getProperties();
    System.out.println(properties);
    assertTrue(properties.size() > 0);
  }
}