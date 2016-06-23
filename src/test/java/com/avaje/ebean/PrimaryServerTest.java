package com.avaje.ebean;

import org.junit.Ignore;
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

  @Ignore
  @Test
  public void testGetPrimaryServerName() throws Exception {

    String primaryServerName = PrimaryServer.getDefaultServerName();
    assertEquals("h2", primaryServerName);
  }

  @Test
  public void testLoadProperties() throws Exception {

    Properties properties = PrimaryServer.getProperties();
    assertTrue(!properties.isEmpty());
  }
}