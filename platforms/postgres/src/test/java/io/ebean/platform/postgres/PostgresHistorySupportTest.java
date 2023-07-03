package io.ebean.platform.postgres;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PostgresHistorySupportTest {

  private final PostgresHistorySupport support = new PostgresHistorySupport();

  @Test
  void getBindCount() {
    assertEquals(support.getBindCount(), 1);
  }

  @Test
  void getAsOfPredicate() {
    String asOfPredicate = support.getAsOfPredicate("t0", "sys_period");
    assertEquals(asOfPredicate, "t0.sys_period @> ?::timestamptz");
  }

  @Test
  void getSysPeriodLower() {
    String lower = support.getSysPeriodLower("t0", "sys_period");
    assertEquals(lower, "lower(t0.sys_period)");
  }

  @Test
  void getSysPeriodUpper() {
    String upper = support.getSysPeriodUpper("t0", "sys_period");
    assertEquals(upper, "upper(t0.sys_period)");
  }

}
