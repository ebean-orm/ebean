package io.ebean.xtest.config.dbplatform;


import io.ebean.platform.mysql.MySqlHistorySupport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MySqlHistorySupportTest {

  private MySqlHistorySupport support = new MySqlHistorySupport();

  @Test
  public void getAsOfPredicate() {

    String asOfPredicate = support.getAsOfPredicate("t0", "sys_period");
    assertEquals(asOfPredicate, "(t0.sys_period_start <= ? and (t0.sys_period_end is null or t0.sys_period_end > ?))");
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
