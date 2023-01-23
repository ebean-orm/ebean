package io.ebean.joda.time;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScalarTypeJodaLocalTimeTest {

  ScalarTypeJodaLocalTime type = new ScalarTypeJodaLocalTime();

  @Test
  public void toJdbcType_toBeanType() {

    LocalTime localTime0 = new LocalTime().withMillisOfSecond(0);
    Object time = type.toJdbcType(localTime0);
    LocalTime localTime1 = type.toBeanType(time);

    assertThat(localTime0).isEqualTo(localTime1);
  }

  @Test
  public void test() {

    long now = System.currentTimeMillis();

    //DateTimeZone timeZone = DateTimeZone.getDefault();
    //ISOChronology instance = ISOChronology.getInstance();

    LocalDateTime ldt1 = new LocalDateTime(now, DateTimeZone.getDefault());
    LocalDateTime ldt2 = new LocalDateTime(now);

    assertEquals(ldt1, ldt2);

    Timestamp ts1 = new Timestamp(ldt1.toDateTime(DateTimeZone.getDefault()).getMillis());
    Timestamp ts2 = new Timestamp(ldt2.toDateTime().getMillis());

    assertEquals(ts1, ts2);

  }


}
