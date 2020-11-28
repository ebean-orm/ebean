package io.ebeaninternal.server.type;

import io.ebean.config.JsonConfig;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.junit.Test;

import java.sql.Timestamp;

import static org.junit.Assert.assertEquals;

public class ScalarTypeJodaLocalDateTimeTest {

  ScalarTypeJodaLocalDateTime type = new ScalarTypeJodaLocalDateTime(JsonConfig.DateTime.ISO8601);

  @Test
  public void testConvertFromTimestamp() throws Exception {

    long now = System.currentTimeMillis();
    Timestamp nowTs = new Timestamp(now);

    LocalDateTime ldt1 = type.convertFromTimestamp(nowTs);
    LocalDateTime ldt2 = localConvertFromTimestamp(nowTs);

    assertEquals(ldt1, ldt2);

    Timestamp ts1 = type.convertToTimestamp(ldt1);
    Timestamp ts2 = localConvertToTimestamp(ldt2);

    assertEquals(ts1, ts2);
  }


  LocalDateTime localConvertFromTimestamp(Timestamp ts) {
    return new LocalDateTime(ts.getTime(), DateTimeZone.getDefault());
  }


  Timestamp localConvertToTimestamp(LocalDateTime t) {
    return new Timestamp(t.toDateTime(DateTimeZone.getDefault()).getMillis());
  }

}
