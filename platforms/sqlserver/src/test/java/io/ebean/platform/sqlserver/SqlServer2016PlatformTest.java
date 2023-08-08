package io.ebean.platform.sqlserver;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Vilmos Nagy  <vilmos.nagy@outlook.com>
 */
public class SqlServer2016PlatformTest {

  @Test
  public void testHistorySupport() {
    SqlServer17Platform platform = new SqlServer17Platform();
    assertTrue(platform.historySupport() instanceof SqlServerHistorySupport);
  }
}
