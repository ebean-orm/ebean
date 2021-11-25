package io.ebeaninternal.server.type;

import io.ebean.config.DatabaseConfig;

public class DatesAndTimesWithJsonMillisTest extends DatesAndTimesTest {
  @Override
  protected void reconfigure(DatabaseConfig config) {
    config.setJsonDate(io.ebean.config.JsonConfig.Date.MILLIS);
    config.setJsonDateTime(io.ebean.config.JsonConfig.DateTime.MILLIS);
  }
}