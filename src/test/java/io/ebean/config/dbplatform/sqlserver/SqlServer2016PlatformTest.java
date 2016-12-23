package io.ebean.config.dbplatform.sqlserver;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * @author Vilmos Nagy  <vilmos.nagy@outlook.com>
 */
public class SqlServer2016PlatformTest {

  @Test
  public void testHistorySupport() {
    SqlServer2016Platform platform = new SqlServer2016Platform();
    assertTrue(platform.getHistorySupport() instanceof SqlServer2016HistorySupport);
  }
}
