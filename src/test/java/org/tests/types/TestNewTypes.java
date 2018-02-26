package org.tests.types;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.junit.Test;
import org.tests.model.types.SomeNewTypesBean;

import java.io.File;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestNewTypes extends BaseTestCase {

  private static final String TEMP_PATH = new File("/tmp").getAbsolutePath();

  @Test
  public void testInsertUpdate() throws  InterruptedException {
    SomeNewTypesBean bean = new SomeNewTypesBean();
    bean.setLocalDate(LocalDate.now());
    bean.setLocalDateTime(LocalDateTime.now());
    bean.setOffsetDateTime(OffsetDateTime.now());
    bean.setZonedDateTime(ZonedDateTime.now());
    bean.setInstant(Instant.now());
    bean.setYear(Year.now());
    bean.setMonth(Month.APRIL);
    bean.setDayOfWeek(DayOfWeek.WEDNESDAY);
    bean.setZoneId(ZoneId.systemDefault());
    bean.setZoneOffset(ZonedDateTime.now().getOffset());
    bean.setYearMonth(YearMonth.of(2014, 9));
    bean.setPath(Paths.get(TEMP_PATH));
    bean.setPeriod(Period.of(4,3,2));


    Ebean.save(bean);

    bean.setYear(Year.now().minusYears(2));
    bean.setMonth(Month.SEPTEMBER);

    Ebean.save(bean);
    Thread.sleep(DB_CLOCK_DELTA); // wait, to ensure that instant < Instant.now()
    List<SomeNewTypesBean> list = Ebean.find(SomeNewTypesBean.class).where().lt("instant", Instant.now()).findList();
    assertTrue(!list.isEmpty());

    list = Ebean.find(SomeNewTypesBean.class).where().le("localDate", LocalDate.now()).findList();
    assertTrue(!list.isEmpty());

    list = Ebean.find(SomeNewTypesBean.class).where().lt("localDateTime", LocalDateTime.now()).findList();
    assertTrue(!list.isEmpty());

    list = Ebean.find(SomeNewTypesBean.class).where().lt("offsetDateTime", OffsetDateTime.now()).findList();
    assertTrue(!list.isEmpty());

    list = Ebean.find(SomeNewTypesBean.class).where().lt("zonedDateTime", ZonedDateTime.now()).findList();
    assertTrue(!list.isEmpty());

    list = Ebean.find(SomeNewTypesBean.class).where().eq("zoneId", ZoneId.systemDefault().getId()).findList();
    assertTrue(!list.isEmpty());

    list = Ebean.find(SomeNewTypesBean.class).where().eq("zoneOffset", ZonedDateTime.now().getOffset()).findList();
    assertTrue(!list.isEmpty());

    list = Ebean.find(SomeNewTypesBean.class).where().le("yearMonth", YearMonth.of(2014, 9)).findList();
    assertTrue(!list.isEmpty());

    list = Ebean.find(SomeNewTypesBean.class).where().le("year", Year.now()).findList();
    assertTrue(!list.isEmpty());

    list = Ebean.find(SomeNewTypesBean.class).where().le("month", Month.SEPTEMBER).findList();
    assertTrue(!list.isEmpty());

    list = Ebean.find(SomeNewTypesBean.class).where().eq("path", Paths.get(TEMP_PATH)).findList();
    assertTrue(!list.isEmpty());

    list = Ebean.find(SomeNewTypesBean.class).where().eq("period", Period.of(4,3,2)).findList();
    assertTrue(!list.isEmpty());

    SomeNewTypesBean fetched = Ebean.find(SomeNewTypesBean.class, bean.getId());

    assertEquals(bean.getZoneId(), fetched.getZoneId());
    assertEquals(bean.getZoneOffset(), fetched.getZoneOffset());
    assertEquals(bean.getMonth(), fetched.getMonth());
    assertEquals(bean.getYear(), fetched.getYear());
    assertEquals(bean.getYearMonth(), fetched.getYearMonth());
    assertEquals(bean.getLocalDate(), fetched.getLocalDate());
    assertThat(fetched.getLocalDateTime()).isEqualToIgnoringNanos(bean.getLocalDateTime());
    assertThat(fetched.getOffsetDateTime()).isEqualToIgnoringNanos(bean.getOffsetDateTime());
    assertEquals(bean.getInstant().toEpochMilli() / 1000, fetched.getInstant().toEpochMilli() / 1000);
    assertEquals(bean.getPath(), fetched.getPath());
    assertEquals(bean.getPeriod(), fetched.getPeriod());


    String asJson = Ebean.json().toJson(fetched);

    SomeNewTypesBean toBean = Ebean.json().toBean(SomeNewTypesBean.class, asJson);

    assertEquals(bean.getZoneId(), toBean.getZoneId());
    assertEquals(bean.getZoneOffset(), toBean.getZoneOffset());
    assertEquals(bean.getMonth(), toBean.getMonth());
    assertEquals(bean.getYear(), toBean.getYear());
    assertEquals(bean.getYearMonth(), toBean.getYearMonth());
    assertEquals(bean.getLocalDate(), toBean.getLocalDate());
    assertThat(toBean.getLocalDateTime()).isEqualToIgnoringNanos(bean.getLocalDateTime());
    assertThat(toBean.getOffsetDateTime()).isEqualToIgnoringNanos(bean.getOffsetDateTime());
    assertEquals(bean.getInstant().toEpochMilli() / 1000, toBean.getInstant().toEpochMilli() / 1000);
    // FIXME: This test fails on Windows with: expected:<\tmp> but was:<C:\tmp>
    assertEquals(bean.getPath(), toBean.getPath());
    assertEquals(bean.getPeriod(), toBean.getPeriod());

  }

  @Test
  public void testInsertNull() {

    SomeNewTypesBean bean = new SomeNewTypesBean();

    Ebean.save(bean);

    SomeNewTypesBean fetched = Ebean.find(SomeNewTypesBean.class, bean.getId());

    assertNull(fetched.getZoneId());
    assertNull(fetched.getZoneOffset());
    assertNull(fetched.getMonth());
    assertNull(fetched.getYear());
    assertNull(fetched.getYearMonth());
    assertNull(fetched.getLocalDate());
    assertNull(fetched.getLocalDateTime());
    assertNull(fetched.getOffsetDateTime());
    assertNull(fetched.getInstant());
    assertNull(fetched.getPath());
    assertNull(fetched.getPeriod());
  }
}
