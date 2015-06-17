package com.avaje.tests.types;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.types.SomeNewTypesBean;
import org.junit.Test;

import java.io.IOException;
import java.time.*;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestNewTypes extends BaseTestCase {

  @Test
  public void testInsertUpdate() throws IOException {

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


    Ebean.save(bean);

    bean.setYear(Year.now().minusYears(2));
    bean.setMonth(Month.SEPTEMBER);

    Ebean.save(bean);

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

    SomeNewTypesBean fetched = Ebean.find(SomeNewTypesBean.class, bean.getId());

    assertEquals(bean.getZoneId(), fetched.getZoneId());
    assertEquals(bean.getZoneOffset(), fetched.getZoneOffset());
    assertEquals(bean.getMonth(), fetched.getMonth());
    assertEquals(bean.getYear(), fetched.getYear());
    assertEquals(bean.getYearMonth(), fetched.getYearMonth());
    assertEquals(bean.getLocalDate(), fetched.getLocalDate());
    assertEquals(bean.getLocalDateTime(), fetched.getLocalDateTime());
    assertEquals(bean.getOffsetDateTime(), fetched.getOffsetDateTime());
    assertEquals(bean.getInstant(), fetched.getInstant());

    String asJson = Ebean.json().toJson(fetched);
    System.out.println(asJson);

    SomeNewTypesBean toBean = Ebean.json().toBean(SomeNewTypesBean.class, asJson);

    assertEquals(bean.getZoneId(), toBean.getZoneId());
    assertEquals(bean.getZoneOffset(), toBean.getZoneOffset());
    assertEquals(bean.getMonth(), toBean.getMonth());
    assertEquals(bean.getYear(), toBean.getYear());
    assertEquals(bean.getYearMonth(), toBean.getYearMonth());
    assertEquals(bean.getLocalDate(), toBean.getLocalDate());
    assertEquals(bean.getLocalDateTime(), toBean.getLocalDateTime());
    assertEquals(bean.getOffsetDateTime(), toBean.getOffsetDateTime());
    assertEquals(bean.getInstant(), toBean.getInstant());

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

  }
}
