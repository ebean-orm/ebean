package io.ebeaninternal.server.type;

import io.ebean.config.JsonConfig;
import org.junit.Test;

import java.sql.Types;
import java.util.Calendar;

import static org.assertj.core.api.Assertions.assertThat;

public class ScalarTypeCalendarTest {

  private ScalarTypeCalendar type = new ScalarTypeCalendar(JsonConfig.DateTime.ISO8601, Types.TIMESTAMP);

  @Test
  public void toJsonISO8601() {

    Calendar instance = Calendar.getInstance();
    String asUtc = type.toJsonISO8601(instance);
    Calendar calendar = type.fromJsonISO8601(asUtc);

    assertThat(instance).isEqualTo(calendar);
  }
}
