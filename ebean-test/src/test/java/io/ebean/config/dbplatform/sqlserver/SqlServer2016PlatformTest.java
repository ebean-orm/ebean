package io.ebean.config.dbplatform.sqlserver;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Vilmos Nagy  <vilmos.nagy@outlook.com>
 */
public class SqlServer2016PlatformTest {

  @Test
  public void testHistorySupport() {
    SqlServer17Platform platform = new SqlServer17Platform();
    assertTrue(platform.getHistorySupport() instanceof SqlServerHistorySupport);
  }
}
