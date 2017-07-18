package io.ebean.config.dbplatform.sqlserver;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Vilmos Nagy  <vilmos.nagy@outlook.com>
 */
public class SqlServerViewHistoryPlatformTest {

  @Test
  public void testHistorySupport() {
    SqlServerPlatform platform = new SqlServerViewHistoryPlatform();
    assertTrue(platform.getHistorySupport() instanceof SqlServerViewHistorySupport);
  }
}
