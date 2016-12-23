package io.ebean.config.dbplatform.sqlserver;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Vilmos Nagy  <vilmos.nagy@outlook.com>
 */
public class SqlServer2016PlatformTest {

  @Test
  public void testHistorySupport() {
    SqlServerPlatform platform = new SqlServerPlatform();
    assertTrue(platform.getHistorySupport() instanceof SqlServerHistorySupport);
  }
}
