package io.ebeaninternal.server.type;

import org.junit.jupiter.api.Disabled;

import io.ebean.config.DatabaseConfig;
@Disabled("Does not work @github")
public class DatesAndTimesWithJsonNanosTest extends DatesAndTimesTest {
  @Override
  protected void reconfigure(DatabaseConfig config) {
    config.setJsonDateTime(io.ebean.config.JsonConfig.DateTime.NANOS);
  }
}