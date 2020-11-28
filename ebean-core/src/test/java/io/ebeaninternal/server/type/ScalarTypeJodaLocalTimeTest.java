package io.ebeaninternal.server.type;

import io.ebean.Ebean;
import org.tests.model.basic.TJodaEntity;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.junit.Test;

import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

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

  @Test
  public void toJson() {

    LocalTime now = new LocalTime();

    TJodaEntity bean = new TJodaEntity();
    bean.setId(42);
    bean.setLocalTime(now);

    String json = Ebean.json().toJson(bean);
    TJodaEntity bean1 = Ebean.json().toBean(TJodaEntity.class, json);

    assertEquals(bean1.getLocalTime(), now);
  }
}
