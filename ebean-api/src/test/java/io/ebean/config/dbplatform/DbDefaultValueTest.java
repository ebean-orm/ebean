package io.ebean.config.dbplatform;

import org.junit.Test;

import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class DbDefaultValueTest {

  @Test
  public void toSqlLiteral_timestamp() {
    assertThat(ts("2001-10-26T21:32:52")).isEqualTo("'2001-10-26T21:32:52'");
    assertThat(ts("2001-10-26T21:32:52+02:00")).isEqualTo("'2001-10-26T21:32:52+02:00'");
    assertThat(ts("2001-10-26T19:32:52Z")).isEqualTo("'2001-10-26T19:32:52Z'");
    assertThat(ts("2001-10-26T19:32:52+00:00")).isEqualTo("'2001-10-26T19:32:52+00:00'");
    assertThat(ts("-2001-10-26T21:32:52")).isEqualTo("'-2001-10-26T21:32:52'");
    assertThat(ts("2001-10-26T21:32:52.12679")).isEqualTo("'2001-10-26T21:32:52.12679'");
  }

  private String ts(String input) {
    return DbDefaultValue.toSqlLiteral(input, OffsetDateTime.class, Types.TIMESTAMP);
  }

  @Test
  public void toSqlLiteral_date() {
    assertThat(date("2001-10-26")).isEqualTo("'2001-10-26'");
    assertThat(date("2001-10-26+02:00")).isEqualTo("'2001-10-26+02:00'");
    assertThat(date("2001-10-26Z")).isEqualTo("'2001-10-26Z'");
    assertThat(date("2001-10-26+00:00")).isEqualTo("'2001-10-26+00:00'");
    assertThat(date("-2001-10-26")).isEqualTo("'-2001-10-26'");
    assertThat(date("-20000-04-01")).isEqualTo("'-20000-04-01'");
  }

  private String date(String input) {
    return DbDefaultValue.toSqlLiteral(input, LocalDate.class, Types.DATE);
  }

  @Test
  public void toSqlLiteral_time() {
    assertThat(time("21:32:52")).isEqualTo("'21:32:52'");
    assertThat(time("21:32:52+02:00")).isEqualTo("'21:32:52+02:00'");
    assertThat(time("19:32:52Z")).isEqualTo("'19:32:52Z'");
    assertThat(time("19:32:52+00:00")).isEqualTo("'19:32:52+00:00'");
    assertThat(time("21:32:52.12679")).isEqualTo("'21:32:52.12679'");
  }

  private String time(String input) {
    return DbDefaultValue.toSqlLiteral(input, LocalTime.class, Types.TIME);
  }

}
