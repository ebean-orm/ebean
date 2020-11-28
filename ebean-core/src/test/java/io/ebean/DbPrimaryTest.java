package io.ebean;

import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.Platform;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DbPrimaryTest extends BaseTestCase {


  @Test
  public void testIsSkipPrimaryServer() {
    DbPrimary.setSkip(true);
    assertTrue(DbPrimary.isSkip());
    DbPrimary.setSkip(false);
    assertFalse(DbPrimary.isSkip());
  }

  @Test
  @ForPlatform(Platform.H2)
  public void testGetPrimaryServerName() {

    String primaryServerName = DbPrimary.getDefaultServerName();
    assertEquals("h2", primaryServerName);
  }

  @Test
  public void testLoadProperties() {

    Properties properties = DbPrimary.getProperties();
    assertTrue(!properties.isEmpty());
  }
}
