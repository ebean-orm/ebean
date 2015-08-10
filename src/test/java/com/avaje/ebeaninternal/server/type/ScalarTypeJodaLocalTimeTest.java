package com.avaje.ebeaninternal.server.type;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.junit.Test;

import java.sql.Timestamp;

import static org.junit.Assert.assertEquals;

public class ScalarTypeJodaLocalTimeTest {

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