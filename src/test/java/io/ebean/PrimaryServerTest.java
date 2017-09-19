package io.ebean;

import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.Platform;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
