package io.ebeaninternal.server.type;

import org.junit.jupiter.api.Disabled;

import io.ebean.config.DatabaseConfig;
@Disabled("Does not work @github")
public class DatesAndTimesWithJsonMillisTest extends DatesAndTimesTest {
  @Override
  protected void reconfigure(DatabaseConfig config) {
    config.setJsonDate(io.ebean.config.JsonConfig.Date.MILLIS);
    config.setJsonDateTime(io.ebean.config.JsonConfig.DateTime.MILLIS);
  }
}