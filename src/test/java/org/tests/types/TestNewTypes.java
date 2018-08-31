package org.tests.types;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.plugin.BeanType;
import io.ebean.plugin.ExpressionPath;

import org.junit.Test;
import org.tests.model.types.SomeNewTypesBean;

import java.io.File;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class TestNewTypes extends BaseTestCase {

  private static final String TEMP_PATH = new File("/tmp").getAbsolutePath();

  @Test
  public void testInsertUpdate() throws  InterruptedException {
    SomeNewTypesBean bean = new SomeNewTypesBean();
    bean.setLocalDate(LocalDate.now());
    bean.setLocalDateTime(LocalDateTime.now());
    bean.setOffsetDateTime(OffsetDateTime.now());
    bean.setZonedDateTime(ZonedDateTime.now());
    bean.setLocalTime(LocalTime.now());
    bean.setInstant(Instant.now());
    bean.setYear(Year.now());
    bean.setMonth(Month.APRIL);
    bean.setDayOfWeek(DayOfWeek.WEDNESDAY);
    bean.setZoneId(ZoneId.systemDefault());
    bean.setZoneOffset(ZonedDateTime.now().getOffset());
    bean.setYearMonth(YearMonth.of(2014, 9));
    bean.setMonthDay(MonthDay.of(9,22));
    bean.setPath(Paths.get(TEMP_PATH));
    bean.setPeriod(Period.of(4,3,2));
    bean.setDuration(Duration.ofMinutes(5));


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

    list = Ebean.find(SomeNewTypesBean.class).where().le("localTime", LocalTime.now()).findList();
    assertTrue(!list.isEmpty());

    list = Ebean.find(SomeNewTypesBean.class).where().eq("zoneId", ZoneId.systemDefault().getId()).findList();
    assertTrue(!list.isEmpty());

    list = Ebean.find(SomeNewTypesBean.class).where().eq("zoneOffset", ZonedDateTime.now().getOffset()).findList();
    assertTrue(!list.isEmpty());

    list = Ebean.find(SomeNewTypesBean.class).where().le("yearMonth", YearMonth.of(2014, 9)).findList();
    assertTrue(!list.isEmpty());

    list = Ebean.find(SomeNewTypesBean.class).where().le("monthDay", MonthDay.of(9,22)).findList();
    assertTrue(!list.isEmpty());

    list = Ebean.find(SomeNewTypesBean.class).where().le("year", Year.now()).findList();
    assertTrue(!list.isEmpty());

    list = Ebean.find(SomeNewTypesBean.class).where().le("month", Month.SEPTEMBER).findList();
    assertTrue(!list.isEmpty());

    list = Ebean.find(SomeNewTypesBean.class).where().eq("path", Paths.get(TEMP_PATH)).findList();
    assertTrue(!list.isEmpty());

    list = Ebean.find(SomeNewTypesBean.class).where().eq("period", Period.of(4,3,2)).findList();
    assertTrue(!list.isEmpty());

    list = Ebean.find(SomeNewTypesBean.class).where().eq("duration", Duration.ofMinutes(5)).findList();
    assertTrue(!list.isEmpty());

    SomeNewTypesBean fetched = Ebean.find(SomeNewTypesBean.class, bean.getId());

    assertEquals(bean.getZoneId(), fetched.getZoneId());
    assertEquals(bean.getZoneOffset(), fetched.getZoneOffset());
    assertEquals(bean.getMonth(), fetched.getMonth());
    assertEquals(bean.getYear(), fetched.getYear());
    assertEquals(bean.getYearMonth(), fetched.getYearMonth());
    assertEquals(bean.getMonthDay(), fetched.getMonthDay());
    assertEquals(bean.getLocalDate(), fetched.getLocalDate());
    assertThat(fetched.getLocalDateTime()).isEqualToIgnoringNanos(bean.getLocalDateTime());
    assertThat(fetched.getOffsetDateTime()).isEqualToIgnoringNanos(bean.getOffsetDateTime());
    assertThat(fetched.getLocalTime().toSecondOfDay()).isEqualTo(bean.getLocalTime().toSecondOfDay());
    assertEquals(bean.getInstant().toEpochMilli() / 1000, fetched.getInstant().toEpochMilli() / 1000);
    assertEquals(bean.getPath(), fetched.getPath());
    assertEquals(bean.getPeriod(), fetched.getPeriod());
    assertEquals(bean.getDuration(), fetched.getDuration());


    String asJson = Ebean.json().toJson(fetched);

    SomeNewTypesBean toBean = Ebean.json().toBean(SomeNewTypesBean.class, asJson);

    assertEquals(bean.getZoneId(), toBean.getZoneId());
    assertEquals(bean.getZoneOffset(), toBean.getZoneOffset());
    assertEquals(bean.getMonth(), toBean.getMonth());
    assertEquals(bean.getYear(), toBean.getYear());
    assertEquals(bean.getYearMonth(), toBean.getYearMonth());
    assertEquals(bean.getMonthDay(), toBean.getMonthDay());
    assertEquals(bean.getLocalDate(), toBean.getLocalDate());
    assertThat(toBean.getLocalDateTime()).isEqualToIgnoringNanos(bean.getLocalDateTime());
    assertThat(toBean.getOffsetDateTime()).isEqualToIgnoringNanos(bean.getOffsetDateTime());
    assertEquals(bean.getLocalTime().toSecondOfDay(), toBean.getLocalTime().toSecondOfDay());
    assertEquals(bean.getInstant().toEpochMilli() / 1000, toBean.getInstant().toEpochMilli() / 1000);
    // FIXME: This test fails on Windows with: expected:<\tmp> but was:<C:\tmp>
    assertEquals(bean.getPath(), toBean.getPath());
    assertEquals(bean.getPeriod(), toBean.getPeriod());
    assertEquals(bean.getDuration(), toBean.getDuration());

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
    assertNull(fetched.getMonthDay());
    assertNull(fetched.getLocalDate());
    assertNull(fetched.getLocalDateTime());
    assertNull(fetched.getOffsetDateTime());
    assertNull(fetched.getLocalTime());
    assertNull(fetched.getInstant());
    assertNull(fetched.getPath());
    assertNull(fetched.getPeriod());
    assertNull(fetched.getDuration());
  }

  @Test
  public void testSetGetPathNonNull() throws Exception {
    SomeNewTypesBean refBean = new SomeNewTypesBean();
    refBean.setLocalDate(LocalDate.now());
    refBean.setLocalDateTime(LocalDateTime.now());
    refBean.setOffsetDateTime(OffsetDateTime.now());
    refBean.setZonedDateTime(ZonedDateTime.now());
    refBean.setLocalTime(LocalTime.now());
    refBean.setInstant(Instant.now());
    refBean.setYear(Year.now());
    refBean.setMonth(Month.APRIL);
    refBean.setDayOfWeek(DayOfWeek.WEDNESDAY);
    refBean.setZoneId(ZoneId.systemDefault());
    refBean.setZoneOffset(ZonedDateTime.now().getOffset());
    refBean.setYearMonth(YearMonth.of(2014, 9));
    refBean.setMonthDay(MonthDay.of( 9, 22));
    refBean.setPath(Paths.get(TEMP_PATH));
    refBean.setPeriod(Period.of(4,3,2));
    refBean.setDuration(Duration.ofMinutes(5));

    testSetGetPath(refBean);
  }

  @Test
  public void testSetGetPathNull() throws Exception {
    SomeNewTypesBean refBean = new SomeNewTypesBean();
    testSetGetPath(refBean);
  }
  private void testSetGetPath(SomeNewTypesBean refBean) {
    SomeNewTypesBean testBean = new SomeNewTypesBean();
    BeanType<SomeNewTypesBean> beanType = Ebean.getDefaultServer().getPluginApi().getBeanType(SomeNewTypesBean.class);
    ExpressionPath localDate = beanType.getExpressionPath("localDate");
    ExpressionPath localDateTime = beanType.getExpressionPath("localDateTime");
    ExpressionPath offsetDateTime = beanType.getExpressionPath("offsetDateTime");
    ExpressionPath zonedDateTime = beanType.getExpressionPath("zonedDateTime");
    ExpressionPath localTime = beanType.getExpressionPath("localTime");
    ExpressionPath instant = beanType.getExpressionPath("instant");
    ExpressionPath year = beanType.getExpressionPath("year");
    ExpressionPath month = beanType.getExpressionPath("month");
    ExpressionPath dayOfWeek = beanType.getExpressionPath("dayOfWeek");
    ExpressionPath zoneId = beanType.getExpressionPath("zoneId");
    ExpressionPath zoneOffset = beanType.getExpressionPath("zoneOffset");
    ExpressionPath yearMonth = beanType.getExpressionPath("yearMonth");
    ExpressionPath monthDay = beanType.getExpressionPath("monthDay");
    ExpressionPath path = beanType.getExpressionPath("path");
    ExpressionPath period = beanType.getExpressionPath("period");
    ExpressionPath duration = beanType.getExpressionPath("duration");

    localDate.pathSet(testBean, refBean.getLocalDate());
    assertThat(localDate.pathGet(testBean)).isEqualTo(refBean.getLocalDate());

    localDateTime.pathSet(testBean, refBean.getLocalDateTime());
    assertThat(localDateTime.pathGet(testBean)).isEqualTo(refBean.getLocalDateTime());

    offsetDateTime.pathSet(testBean, refBean.getOffsetDateTime());
    assertThat(offsetDateTime.pathGet(testBean)).isEqualTo(refBean.getOffsetDateTime());

    zonedDateTime.pathSet(testBean, refBean.getZonedDateTime());
    assertThat(zonedDateTime.pathGet(testBean)).isEqualTo(refBean.getZonedDateTime());

    localTime.pathSet(testBean, refBean.getLocalTime());
    assertThat(localTime.pathGet(testBean)).isEqualTo(refBean.getLocalTime());

    instant.pathSet(testBean, refBean.getInstant());
    assertThat(instant.pathGet(testBean)).isEqualTo(refBean.getInstant());

    year.pathSet(testBean, refBean.getYear());
    assertThat(year.pathGet(testBean)).isEqualTo(refBean.getYear());

    month.pathSet(testBean, refBean.getMonth());
    assertThat(month.pathGet(testBean)).isEqualTo(refBean.getMonth());

    dayOfWeek.pathSet(testBean, refBean.getDayOfWeek());
    assertThat(dayOfWeek.pathGet(testBean)).isEqualTo(refBean.getDayOfWeek());

    zoneId.pathSet(testBean, refBean.getZoneId());
    assertThat(zoneId.pathGet(testBean)).isEqualTo(refBean.getZoneId());

    zoneOffset.pathSet(testBean, refBean.getZoneOffset());
    assertThat(zoneOffset.pathGet(testBean)).isEqualTo(refBean.getZoneOffset());

    yearMonth.pathSet(testBean, refBean.getYearMonth());
    assertThat(yearMonth.pathGet(testBean)).isEqualTo(refBean.getYearMonth());

    monthDay.pathSet(testBean, refBean.getMonthDay());
    assertThat(monthDay.pathGet(testBean)).isEqualTo(refBean.getMonthDay());

    path.pathSet(testBean, refBean.getPath());
    assertThat(path.pathGet(testBean)).isEqualTo(refBean.getPath());

    period.pathSet(testBean, refBean.getPeriod());
    assertThat(period.pathGet(testBean)).isEqualTo(refBean.getPeriod());

    duration.pathSet(testBean, refBean.getDuration());
    assertThat(duration.pathGet(testBean)).isEqualTo(refBean.getDuration());

    Ebean.save(refBean);
    Ebean.save(testBean);
  }

}
