package io.ebean.platform.hana;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HanaHistorySupportTest {

  private final HanaHistorySupport support = new HanaHistorySupport();

  @Test
  void getAsOfPredicate() {
    String asOfPredicate = support.getAsOfPredicate("t0", "sys_period");
    assertNull(asOfPredicate);
  }

  @Test
  void getAsOfViewSuffix() {
    String asOfViewSuffix = support.getAsOfViewSuffix("_with_history");
    assertEquals(asOfViewSuffix, " for system_time as of ?");
  }

  @Test
  void getVersionsBetweenSuffix() {
    String asOfViewSuffix = support.getVersionsBetweenSuffix("_with_history");
    assertEquals(asOfViewSuffix, " for system_time between ? and ?");
  }

  @Test
  void getLower() {
    String lower = support.getSysPeriodLower("t0", "sys_period");
    assertEquals(lower, "t0.sys_period_start");
  }

  @Test
  void getUpper() {
    String upper = support.getSysPeriodUpper("t0", "sys_period");
    assertEquals(upper, "t0.sys_period_end");
  }
}
