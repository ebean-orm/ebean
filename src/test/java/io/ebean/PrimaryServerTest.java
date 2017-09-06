package io.ebean;

import io.ebean.PrimaryServer;
import io.ebean.annotation.ForPlatform;

import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

public class PrimaryServerTest extends BaseTestCase {


  @Test
  public void testIsSkipPrimaryServer() throws Exception {
    PrimaryServer.setSkip(true);
    assertTrue(PrimaryServer.isSkip());
    PrimaryServer.setSkip(false);
    assertFalse(PrimaryServer.isSkip());
  }

  @Test
  @ForPlatform(Platform.H2)
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
