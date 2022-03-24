package io.ebean;

import io.ebean.xtest.BaseTestCase;
import io.ebean.xtest.ForPlatform;
import io.ebean.annotation.Platform;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

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
