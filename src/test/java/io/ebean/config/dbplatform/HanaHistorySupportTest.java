package io.ebean.config.dbplatform;

import io.ebean.config.dbplatform.hana.HanaHistorySupport;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class HanaHistorySupportTest {

  private HanaHistorySupport support = new HanaHistorySupport();

  @Test
  public void getAsOfPredicate() {

    String asOfPredicate = support.getAsOfPredicate("t0", "sys_period");
    assertNull(asOfPredicate);
  }

  @Test
  public void getAsOfViewSuffix() {

    String asOfViewSuffix = support.getAsOfViewSuffix("_with_history");
    assertEquals(asOfViewSuffix, " for system_time as of ?");
  }

  @Test
  public void getVersionsBetweenSuffix() {

    String asOfViewSuffix = support.getVersionsBetweenSuffix("_with_history");
    assertEquals(asOfViewSuffix, " for system_time between ? and ?");
  }

  @Test
  public void getLower() throws Exception {

    String lower = support.getSysPeriodLower("t0", "sys_period");
    assertEquals(lower, "t0.sys_period_start");
  }

  @Test
  public void getUpper() throws Exception {

    String upper = support.getSysPeriodUpper("t0", "sys_period");
    assertEquals(upper, "t0.sys_period_end");
  }
}
