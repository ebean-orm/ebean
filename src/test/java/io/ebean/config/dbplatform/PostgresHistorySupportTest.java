package io.ebean.config.dbplatform;

import io.ebean.config.dbplatform.postgres.PostgresHistorySupport;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PostgresHistorySupportTest {

  private PostgresHistorySupport support = new PostgresHistorySupport();

  @Test
  public void getBindCount() throws Exception {

    assertEquals(support.getBindCount(), 1);
  }

  @Test
  public void getAsOfPredicate() throws Exception {

    String asOfPredicate = support.getAsOfPredicate("t0", "sys_period");
    assertEquals(asOfPredicate, "t0.sys_period @> ?::timestamptz");
  }

  @Test
  public void getSysPeriodLower() throws Exception {

    String lower = support.getSysPeriodLower("t0", "sys_period");
    assertEquals(lower, "lower(t0.sys_period)");
  }

  @Test
  public void getSysPeriodUpper() throws Exception {

    String upper = support.getSysPeriodUpper("t0", "sys_period");
    assertEquals(upper, "upper(t0.sys_period)");
  }

}
