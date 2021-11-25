package io.ebeaninternal.server.type;

import io.ebean.config.DatabaseConfig;

public class DatesAndTimesWithNanosTest extends DatesAndTimesTest {
  @Override
  protected void reconfigure(DatabaseConfig config) {
    config.setLocalTimeWithNanos(true);
  }
}