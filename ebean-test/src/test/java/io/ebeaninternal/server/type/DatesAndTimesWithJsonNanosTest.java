package io.ebeaninternal.server.type;

import io.ebean.config.DatabaseConfig;

public class DatesAndTimesWithJsonNanosTest extends DatesAndTimesTest {
  @Override
  protected void reconfigure(DatabaseConfig config) {
    config.setJsonDateTime(io.ebean.config.JsonConfig.DateTime.NANOS);
  }
}