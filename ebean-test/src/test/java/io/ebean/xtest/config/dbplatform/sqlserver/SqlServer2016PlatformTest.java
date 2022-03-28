package io.ebean.xtest.config.dbplatform.sqlserver;

import io.ebean.platform.sqlserver.SqlServer17Platform;
import io.ebean.platform.sqlserver.SqlServerHistorySupport;
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
