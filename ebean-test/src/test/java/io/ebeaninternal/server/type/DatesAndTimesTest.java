package io.ebeaninternal.server.type;

import static java.lang.String.format;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import javax.sql.DataSource;

import org.assertj.core.api.SoftAssertions;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.tests.model.basic.MDateTime;

import io.ebean.DB;
import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;
import io.ebean.core.type.ScalarType;
import io.ebean.plugin.ExpressionPath;
import io.ebean.plugin.Property;
import io.ebean.text.PathProperties;
import io.ebean.text.json.JsonWriteOptions;
import io.ebean.util.CamelCaseHelper;
import io.ebeaninternal.server.deploy.BeanProperty;

@TestInstance(Lifecycle.PER_CLASS)
public class DatesAndTimesTest {
 
  private Database db;
  private TimeZone tz;
  private DatabaseConfig config;
  private SoftAssertions softly;

  private String json;
  private String formatted;
  private long millis;

  @BeforeEach
  public void startTest() {
    softly = new SoftAssertions();
    tz = TimeZone.getDefault();
    if (db == null) {
      db = createServer("GMT"); // test uses GMT database
    } else {
      restartServer(null, "GMT");
    }
    json = null;
    formatted = null;
    millis = 0;
  }
  
  @AfterEach
  public void stopTest() {
    setJavaTimeZone(tz);
    softly.assertAll();
  }

  @AfterAll
  public void shutdown() {
    if (db != null) {
      db.find(MDateTime.class).delete();
      db.shutdown(false, false);
    }
  }
  
  private void restartServer(String javaTimeZone, String dbTimeZone) {
    DataSource existingDs = db.dataSource();
    DataSource existingRoDs = db.readOnlyDataSource();
    db.shutdown(false, false);
    if (javaTimeZone != null) {
      setJavaTimeZone(TimeZone.getTimeZone(javaTimeZone));
    }
    db = createServer(dbTimeZone);
  }

  private void setJavaTimeZone(TimeZone newTz) {
    TimeZone.setDefault(newTz);
    DateTimeZone.setDefault(DateTimeZone.forTimeZone(newTz));
    org.h2.util.DateTimeUtils.resetCalendar();
  }
  
  private Database createServer(String dbTimeZone) {
    
    // we create a clone for the current default server
    config = new DatabaseConfig();
    config.setName(DB.getDefault().name());
    config.loadFromProperties();
    config.setDataSource(DB.getDefault().dataSource());
    config.setReadOnlyDataSource(DB.getDefault().readOnlyDataSource());
    config.setDdlExtra(false);
    config.setDdlGenerate(false);
    config.setDdlRun(false);
    config.setDefaultServer(false);
    config.setRegister(false);
    config.setChangeLogAsync(false);
    config.addClass(MDateTime.class);
    
    config.setDumpMetricsOnShutdown(false);
    config.setDataTimeZone(dbTimeZone);
    reconfigure(config);

    return DatabaseFactory.create(config);
  }

  protected void reconfigure(DatabaseConfig config) {
    
  }

  @Test
  public void testLocalTime() {
    if (config.isLocalTimeWithNanos()) {
      LocalTime lt = LocalTime.of(5, 15, 15,123456789);
      doTest("localTime", lt, String.valueOf(lt.toNanoOfDay())); 
      softly.assertThat(json).isEqualTo("{\"localTime\":\"05:15:15.123456789\"}");
      softly.assertThat(formatted).isEqualTo("05:15:15.123456789");
      return;
    }
    // localTimes are never converted, when read or written to database
    doTest("localTime", LocalTime.of(5, 15, 15), "05:15:15");
    softly.assertThat(json).isEqualTo("{\"localTime\":\"05:15:15\"}");
    softly.assertThat(formatted).isEqualTo("05:15:15");
    
    doTest("localTime", LocalTime.of(0, 0, 0), "00:00:00");
    doTest("localTime", LocalTime.of(23, 59, 59), "23:59:59");

    // it does not matter in which timezone the server or java is!
    restartServer("PST", "Europe/Berlin");
    doTest("localTime", LocalTime.of(5, 15, 15), "05:15:15");
    doTest("localTime", LocalTime.of(0, 0, 0), "00:00:00");
    doTest("localTime", LocalTime.of(23, 59, 59), "23:59:59");

    restartServer("Europe/Berlin", "PST");
    doTest("localTime", LocalTime.of(5, 15, 15), "05:15:15");
    doTest("localTime", LocalTime.of(0, 0, 0), "00:00:00");
    doTest("localTime", LocalTime.of(23, 59, 59), "23:59:59");

  }

  @Test
  public void testJodaLocalTime() {
    // localTimes are never converted, when read or written to database
    doTest("jodaLocalTime", org.joda.time.LocalTime.parse("05:15:15"), "05:15:15");
    softly.assertThat(json).isEqualTo("{\"jodaLocalTime\":\"05:15:15.000\"}");
    softly.assertThat(formatted).isEqualTo("05:15:15.000");
    
    doTest("jodaLocalTime", org.joda.time.LocalTime.parse("00:00:00"), "00:00:00");
    doTest("jodaLocalTime", org.joda.time.LocalTime.parse("23:59:59"), "23:59:59");

    // it does not matter in which timezone the server or java is!
    restartServer("PST", "Europe/Berlin");
    doTest("jodaLocalTime", org.joda.time.LocalTime.parse("05:15:15"), "05:15:15");
    doTest("jodaLocalTime", org.joda.time.LocalTime.parse("00:00:00"), "00:00:00");
    doTest("jodaLocalTime", org.joda.time.LocalTime.parse("23:59:59"), "23:59:59");

    restartServer("Europe/Berlin", "PST");
    doTest("jodaLocalTime", org.joda.time.LocalTime.parse("05:15:15"), "05:15:15");
    doTest("jodaLocalTime", org.joda.time.LocalTime.parse("00:00:00"), "00:00:00");
    doTest("jodaLocalTime", org.joda.time.LocalTime.parse("23:59:59"), "23:59:59");

  }

  @Test
  public void testLocalDate() {

    // Test with DST and no DST date (in germany)
    doTest("localDate", LocalDate.parse("2021-11-21"), "2021-11-21");
    softly.assertThat(formatted).isEqualTo(String.valueOf(1637452800000L - 3_600_000L));
    softly.assertThat(millis).isEqualTo(1637452800000L); // 00:00 in GMT
    
    if (config.getJsonDate() == io.ebean.config.JsonConfig.Date.ISO8601) {
      softly.assertThat(json).isEqualTo("{\"localDate\":\"2021-11-21\"}");
    } else {
      softly.assertThat(json).isEqualTo("{\"localDate\":1637452800000}"); // 21-nov 00:00 GMT
    }
  
    doTest("localDate", LocalDate.parse("1970-01-01"), "1970-01-01");
    // softly.assertThat(formatted).isEqualTo("0");
    softly.assertThat(formatted).isEqualTo("-3600000");
    softly.assertThat(millis).isEqualTo(0L); 
    
    doTest("localDate", LocalDate.parse("1969-12-31"), "1969-12-31");
    softly.assertThat(formatted).isEqualTo("-90000000");
    softly.assertThat(millis).isEqualTo(-86400000L); 
    if (config.getJsonDate() == io.ebean.config.JsonConfig.Date.ISO8601) {
      softly.assertThat(json).isEqualTo("{\"localDate\":\"1969-12-31\"}");
    } else {
      softly.assertThat(json).isEqualTo("{\"localDate\":-86400000}");
    }

    doTest("localDate", LocalDate.parse("2021-08-21"), "2021-08-21");

//    restartServer("PST", "Europe/Berlin");
//    doTest("localDate", LocalDate.parse("2021-11-21"), "2021-11-21");
//    doTest("localDate", LocalDate.parse("2021-08-21"), "2021-08-21");

    restartServer("Europe/Berlin", "PST");
    doTest("localDate", LocalDate.parse("2021-11-21"), "2021-11-21");
    doTest("localDate", LocalDate.parse("2021-08-21"), "2021-08-21");

  }

  @Test
  public void testJodaLocalDate() {

    // Test with DST and no DST date (in germany)
    doTest("jodaLocalDate", org.joda.time.LocalDate.parse("2021-11-21"), "2021-11-21");
    if (config.getJsonDate() == io.ebean.config.JsonConfig.Date.ISO8601) {
      softly.assertThat(json).isEqualTo("{\"jodaLocalDate\":\"2021-11-21\"}");
    } else {
      softly.assertThat(json).isEqualTo("{\"jodaLocalDate\":1637452800000}"); // GMT: 00:00
    }

    
    doTest("jodaLocalDate", org.joda.time.LocalDate.parse("2021-08-21"), "2021-08-21");

    doTest("jodaLocalDate", org.joda.time.LocalDate.parse("1970-01-01"), "1970-01-01");
    //softly.assertThat(formatted).isEqualTo("0");
    softly.assertThat(formatted).isEqualTo("-3600000");
    softly.assertThat(millis).isEqualTo(0L); 
    
    doTest("jodaLocalDate", org.joda.time.LocalDate.parse("1969-12-31"), "1969-12-31");
    softly.assertThat(formatted).isEqualTo("-90000000");
    softly.assertThat(millis).isEqualTo(-86400000L); 
    
    //    restartServer("PST", "Europe/Berlin");
    //    doTest("jodaLocalDate", org.joda.time.LocalDate.parse("2021-11-21"), "2021-11-21");
    //    doTest("jodaLocalDate", org.joda.time.LocalDate.parse("2021-08-21"), "2021-08-20");
   
    restartServer("Europe/Berlin", "PST");
    doTest("jodaLocalDate", org.joda.time.LocalDate.parse("2021-11-21"), "2021-11-21");
    doTest("jodaLocalDate", org.joda.time.LocalDate.parse("2021-08-21"), "2021-08-21");

  }

  @Test
  public void testCalendar() {

    restartServer("GMT", "GMT");
    Calendar cal = Calendar.getInstance();
    cal.clear();
    cal.set(2021, 8 - 1, 21, 5, 15, 15); // month 0-based!

    softly.assertThat(cal.toInstant()).isEqualTo(Instant.parse("2021-08-21T05:15:15Z"));

    doTest("propCalendar", cal, "2021-08-21 05:15:15");
    if (config.getJsonDateTime() == io.ebean.config.JsonConfig.DateTime.ISO8601) {
      softly.assertThat(json).isEqualTo("{\"propCalendar\":\"2021-08-21T05:15:15.000Z\"}");
    } else if (config.getJsonDateTime() == io.ebean.config.JsonConfig.DateTime.MILLIS) {
      softly.assertThat(json).isEqualTo("{\"propCalendar\":1629522915000}");
    } else {
      // softly.assertThat(json).isEqualTo("{\"propCalendar\":1629522915.000000000}"); // 05:15 GMT
      softly.assertThat(json).isEqualTo("{\"propCalendar\":1629522915000}"); // FIXME: this are millis!
    }
    
    cal.clear();
    cal.setTimeInMillis(-1);
    doTest("propCalendar", cal, "1969-12-31 23:59:59.999");
    softly.assertThat(formatted).isEqualTo("-1");
    softly.assertThat(millis).isEqualTo(-1L); 
    
    if (config.getJsonDateTime() == io.ebean.config.JsonConfig.DateTime.ISO8601) {
      softly.assertThat(json).isEqualTo("{\"propCalendar\":\"1969-12-31T23:59:59.999Z\"}");
    } else if (config.getJsonDateTime() == io.ebean.config.JsonConfig.DateTime.MILLIS) {
      softly.assertThat(json).isEqualTo("{\"propCalendar\":-1}");
    } else {
      // softly.assertThat(json).isEqualTo("{\"propCalendar\":-0.001000000}");
      softly.assertThat(json).isEqualTo("{\"propCalendar\":-1}");  // FIXME: this are millis! 
    }
    
    // test in PST time zone
    restartServer("PST", "GMT");
    cal = Calendar.getInstance();
    cal.setTimeInMillis(0); // clear
    cal.set(2021, 8 - 1, 20, 22, 15, 15); // month 0-based!

    softly.assertThat(cal.toInstant()).isEqualTo(Instant.parse("2021-08-21T05:15:15Z"));

    doTest("propCalendar", cal, "2021-08-21 05:15:15");
    if (config.getJsonDateTime() == io.ebean.config.JsonConfig.DateTime.ISO8601) {
      softly.assertThat(json).isEqualTo("{\"propCalendar\":\"2021-08-21T05:15:15.000Z\"}");
    } else if (config.getJsonDateTime() == io.ebean.config.JsonConfig.DateTime.MILLIS) {
      softly.assertThat(json).isEqualTo("{\"propCalendar\":1629522915000}");
    } else {
      // softly.assertThat(json).isEqualTo("{\"propCalendar\":1629522915.000000000}");
      softly.assertThat(json).isEqualTo("{\"propCalendar\":1629522915000}"); // FIXME: this are millis! 
    }
  }

  @Test
  public void testInstant() {

    // Test with DST and no DST date (in germany)
    doTest("propInstant", Instant.parse("2021-11-21T05:15:15Z"), "2021-11-21 05:15:15");
    if (config.getJsonDateTime() == io.ebean.config.JsonConfig.DateTime.ISO8601) {
      softly.assertThat(json).isEqualTo("{\"propInstant\":\"2021-11-21T05:15:15Z\"}");
    } else if (config.getJsonDateTime()  == io.ebean.config.JsonConfig.DateTime.MILLIS) {
      softly.assertThat(json).isEqualTo("{\"propInstant\":1637471715000}");
    } else {
      softly.assertThat(json).isEqualTo("{\"propInstant\":1637471715.000000000}"); // 05:15 GMT
    }

    doTest("propInstant", Instant.parse("1970-01-01T00:00:00Z"), "1970-01-01 00:00:00");
    softly.assertThat(formatted).isEqualTo("0");
    softly.assertThat(millis).isEqualTo(0L); 
    
    doTest("propInstant", Instant.parse("2021-08-21T05:15:15Z"), "2021-08-21 05:15:15");

    restartServer("PST", "GMT");

    doTest("propInstant", Instant.parse("2021-11-21T05:15:15Z"), "2021-11-21 05:15:15");
    doTest("propInstant", Instant.parse("2021-08-21T05:15:15Z"), "2021-08-21 05:15:15");
  }

  @Test
  public void testJodaDateTime() {

    // Test with DST and no DST date (in germany)
    doTest("jodaDateTime", org.joda.time.DateTime.parse("2021-11-21T05:15:15Z"), "2021-11-21 05:15:15");
    if (config.getJsonDateTime() == io.ebean.config.JsonConfig.DateTime.ISO8601) {
      softly.assertThat(json).isEqualTo("{\"jodaDateTime\":\"2021-11-21T05:15:15.000Z\"}");
    } else {
      softly.assertThat(json).isEqualTo("{\"jodaDateTime\":1637471715000}");
    }
    
    doTest("jodaDateTime", org.joda.time.DateTime.parse("1970-01-01T00:00:00Z"), "1970-01-01 00:00:00");
    softly.assertThat(formatted).isEqualTo("0");
    softly.assertThat(millis).isEqualTo(0L); 

    restartServer("PST", "GMT");

    doTest("jodaDateTime", org.joda.time.DateTime.parse("2021-11-21T05:15:15Z"), "2021-11-21 05:15:15");
    doTest("jodaDateTime", org.joda.time.DateTime.parse("2021-08-21T05:15:15Z"), "2021-08-21 05:15:15");
  }

  @Test
  public void testLocalDateTime() {

    // Test with DST and no DST date (in germany)
    // CHECKME: LocalDateTimes are stored as instant in DB. (that may be justifiable)
    doTest("localDateTime", LocalDateTime.parse("2021-11-21T05:15:15"), "2021-11-21 04:15:15");
    // CHECKME: getJsonDateTime is not respected at LocalDateTime
    softly.assertThat(json).isEqualTo("{\"localDateTime\":\"2021-11-21T05:15:15\"}");
//    if (config.getJsonDateTime() == io.ebean.config.JsonConfig.DateTime.ISO8601) {
//    } else if (config.getJsonDateTime() == io.ebean.config.JsonConfig.DateTime.MILLIS) {
//      softly.assertThat(json).isEqualTo("{\"localDateTime\":1637471715000}");
//    } else {
//      softly.assertThat(json).isEqualTo("{\"localDateTime\":1637471715.000000000}");
//    }
    softly.assertThat(formatted).isEqualTo("2021-11-21T05:15:15"); // WHY is this not formatted in millis
    // CHECKME: TZ conversion may be justifiable
    softly.assertThat(millis).isEqualTo(1637471715000L - 3_600_000L); 
    
    // doTest("localDateTime", LocalDateTime.parse("1970-01-01T00:00:00"), "1970-01-01 00:00:00");
    // CHECKME: LocalDateTimes are stored as instant in DB. (that may be justifiable)
    doTest("localDateTime", LocalDateTime.parse("1970-01-01T00:00:00"), "1969-12-31 23:00:00");
    softly.assertThat(formatted).isEqualTo("1970-01-01T00:00");
    softly.assertThat(millis).isEqualTo(0L - 3_600_000L); 

    restartServer("PST", "Europe/Berlin");
    doTest("localDateTime", LocalDateTime.parse("2021-11-21T05:15:15"), "2021-11-21 14:15:15");
    doTest("localDateTime", LocalDateTime.parse("2021-08-21T05:15:15"), "2021-08-21 14:15:15");

    restartServer("Europe/Berlin", "PST");
    doTest("localDateTime", LocalDateTime.parse("2021-11-21T05:15:15"), "2021-11-20 20:15:15");
    doTest("localDateTime", LocalDateTime.parse("2021-08-21T05:15:15"), "2021-08-20 20:15:15");

  }

  @Test
  public void testJodaLocalDateTime() {

    // Test with DST and no DST date (in germany)
    doTest("jodaLocalDateTime", org.joda.time.LocalDateTime.parse("2021-11-21T05:15:15"), "2021-11-21 04:15:15");
    if (config.getJsonDateTime() == io.ebean.config.JsonConfig.DateTime.ISO8601) {
      softly.assertThat(json).isEqualTo("{\"jodaLocalDateTime\":\"2021-11-21T05:15:15.000\"}");
    } else if (config.getJsonDateTime() == io.ebean.config.JsonConfig.DateTime.MILLIS) {
      softly.assertThat(json).isEqualTo("{\"jodaLocalDateTime\":1637468115000}"); // 05:15:15 Europe/Berlin correct?
    } else {
      // softly.assertThat(json).isEqualTo("{\"jodaLocalDateTime\":1637471715.000000000}"); // 05:15:15 GMT
      softly.assertThat(json).isEqualTo("{\"jodaLocalDateTime\":1637468115000}"); // Fixme: Joda uses millis here
    }
    
    doTest("jodaLocalDateTime", org.joda.time.LocalDateTime.parse("2021-08-21T05:15:15"), "2021-08-21 03:15:15");

    restartServer("PST", "Europe/Berlin");
    doTest("jodaLocalDateTime", org.joda.time.LocalDateTime.parse("2021-11-21T05:15:15"), "2021-11-21 14:15:15");
    doTest("jodaLocalDateTime", org.joda.time.LocalDateTime.parse("2021-08-21T05:15:15"), "2021-08-21 14:15:15");

    restartServer("Europe/Berlin", "PST");
    doTest("jodaLocalDateTime", org.joda.time.LocalDateTime.parse("2021-11-21T05:15:15"), "2021-11-20 20:15:15");
    doTest("jodaLocalDateTime", org.joda.time.LocalDateTime.parse("2021-08-21T05:15:15"), "2021-08-20 20:15:15");

  }

  @Test
  public void testJodaDateMidnight() {

    // Test with DST and no DST date (in germany)
    doTest("jodaDateMidnight", org.joda.time.DateMidnight.parse("2021-11-21"), "2021-11-21");
    if (config.getJsonDate() == io.ebean.config.JsonConfig.Date.ISO8601) {
      softly.assertThat(json).isEqualTo("{\"jodaDateMidnight\":\"2021-11-21\"}");
    } else {
      softly.assertThat(json).isEqualTo("{\"jodaDateMidnight\":1637449200000}");
    }
    
    doTest("jodaDateMidnight", org.joda.time.DateMidnight.parse("2021-08-21"), "2021-08-21");

    restartServer("PST", "Europe/Berlin");
    doTest("jodaDateMidnight", org.joda.time.DateMidnight.parse("2021-11-21"), "2021-11-21");
    doTest("jodaDateMidnight", org.joda.time.DateMidnight.parse("2021-08-21"), "2021-08-21");

    restartServer("Europe/Berlin", "PST");
    doTest("jodaDateMidnight", org.joda.time.DateMidnight.parse("2021-11-21"), "2021-11-21");
    doTest("jodaDateMidnight", org.joda.time.DateMidnight.parse("2021-08-21"), "2021-08-21");
  }

  @Test
  public void testYearMonth() {

    // Test with DST and no DST date (in germany)
    doTest("propYearMonth", YearMonth.of(2020, 2), "2020-02-01");
    if (config.getJsonDate() == io.ebean.config.JsonConfig.Date.ISO8601) {
      softly.assertThat(json).isEqualTo("{\"propYearMonth\":\"2020-02-01\"}");
    } else {
      softly.assertThat(json).isEqualTo("{\"propYearMonth\":1580515200000}"); // 00:00 GMT
    }
    doTest("propYearMonth", YearMonth.of(2020, 3), "2020-03-01");
    doTest("propYearMonth", YearMonth.of(2020, 4), "2020-04-01");

  }
  @Test
  public void testYear() {

    doTest("propYear", Year.of(2020), "2020");

  }
  
  @Test
  public void testMonthDay() {

    // Test with DST and no DST date (in germany)
    doTest("propMonthDay", MonthDay.of(11, 21), "2000-11-21");
    softly.assertThat(json).isEqualTo("{\"propMonthDay\":\"--11-21\"}");
    doTest("propMonthDay", MonthDay.of(8, 21), "2000-08-21");
    doTest("propMonthDay", MonthDay.of(2, 29), "2000-02-29"); // leap year check

    restartServer("PST", "Europe/Berlin");
    doTest("propMonthDay", MonthDay.of(11, 21), "2000-11-21");
    doTest("propMonthDay", MonthDay.of(8, 21), "2000-08-21");
    doTest("propMonthDay", MonthDay.of(2, 29), "2000-02-29"); // leap year check

    restartServer("Europe/Berlin", "PST");
    doTest("propMonthDay", MonthDay.of(11, 21), "2000-11-21");
    doTest("propMonthDay", MonthDay.of(8, 21), "2000-08-21");
    doTest("propMonthDay", MonthDay.of(2, 29), "2000-02-29"); // leap year check
  }

  @Test
  public void testSqlDate() {
    // localTimes are never converted, when read or written to database
    doTest("sqlDate", new java.sql.Date(2021 - 1900, 11 - 1, 21), "2021-11-21");
    if (config.getJsonDate() == io.ebean.config.JsonConfig.Date.ISO8601) {
      softly.assertThat(json).isEqualTo("{\"sqlDate\":\"2021-11-21\"}");
    } else {
      softly.assertThat(json).isEqualTo("{\"sqlDate\":1637449200000}"); // 00:00 Europe/Berlin. correct?
    }

    doTest("sqlDate", new java.sql.Date(2021 - 1900, 8 - 1, 21), "2021-08-21");

    // it does not matter in which timezone the server or java is!
    restartServer("PST", "Europe/Berlin");
    doTest("sqlDate", new java.sql.Date(2021 - 1900, 11 - 1, 21), "2021-11-21");
    doTest("sqlDate", new java.sql.Date(2021 - 1900, 8 - 1, 21), "2021-08-21");

    restartServer("Europe/Berlin", "PST");
    doTest("sqlDate", new java.sql.Date(2021 - 1900, 11 - 1, 21), "2021-11-21");
    doTest("sqlDate", new java.sql.Date(2021 - 1900, 8 - 1, 21), "2021-08-21");

  }

  @Test
  public void testSqlTime() {

    // Test with DST and no DST date (in germany)
    doTest("sqlTime", new java.sql.Time(5, 15, 15), "05:15:15");
    softly.assertThat(json).isEqualTo("{\"sqlTime\":\"05:15:15\"}");
    
    doTest("sqlTime", new java.sql.Time(0, 0, 0), "00:00:00");
    doTest("sqlTime", new java.sql.Time(23, 59, 59), "23:59:59");

    restartServer("PST", "Europe/Berlin");
    doTest("sqlTime", new java.sql.Time(5, 15, 15), "05:15:15");
    doTest("sqlTime", new java.sql.Time(0, 0, 0), "00:00:00");
    doTest("sqlTime", new java.sql.Time(23, 59, 59), "23:59:59");

    restartServer("Europe/Berlin", "PST");
    doTest("sqlTime", new java.sql.Time(5, 15, 15), "05:15:15");
    doTest("sqlTime", new java.sql.Time(0, 0, 0), "00:00:00");
    doTest("sqlTime", new java.sql.Time(23, 59, 59), "23:59:59");
  }
  
  @Test
  public void testTimestamp() {
    restartServer("PST", "PST"); // java & db in same TZ
    doTest("propTimestamp", new Timestamp(2021 - 1900, 11 - 1, 21, 5, 15, 15, 0), "2021-11-21 05:15:15");
    if (config.getJsonDateTime() == io.ebean.config.JsonConfig.DateTime.ISO8601) {
      softly.assertThat(json).isEqualTo("{\"propTimestamp\":\"2021-11-21T13:15:15Z\"}");
    } else if (config.getJsonDateTime()  == io.ebean.config.JsonConfig.DateTime.MILLIS) {
      softly.assertThat(json).isEqualTo("{\"propTimestamp\":1637500515000}");
    } else {
      // softly.assertThat(json).isEqualTo("{\"propTimestamp\":1637500515.000000000}");
      softly.assertThat(json).isEqualTo("{\"propTimestamp\":1637500515000}"); // FIXME: timestamp uses lillis here!
    }

    
    restartServer("Europe/Berlin", "GMT"); // go to germany
    doTest("propTimestamp", new Timestamp(2021 - 1900, 11 - 1, 21, 6, 15, 15, 0), "2021-11-21 05:15:15");
    doTest("propTimestamp", new Timestamp(2021 - 1900, 8 - 1, 21, 7, 15, 15, 0), "2021-08-21 05:15:15"); // Check DST
    
    doTest("propTimestamp", new Timestamp(2021 - 1900, 3 - 1, 27, 23, 0, 0, 0), "2021-03-27 22:00:00");
    doTest("propTimestamp", new Timestamp(2021 - 1900, 3 - 1, 28, 0, 0, 0, 0), "2021-03-27 23:00:00");
    doTest("propTimestamp", new Timestamp(2021 - 1900, 3 - 1, 28, 1, 0, 0, 0), "2021-03-28 00:00:00");
    doTest("propTimestamp", new Timestamp(2021 - 1900, 3 - 1, 28, 2, 0, 0, 0), "2021-03-28 01:00:00");
    doTest("propTimestamp", new Timestamp(2021 - 1900, 3 - 1, 28, 3, 0, 1, 0), "2021-03-28 01:00:01");
    doTest("propTimestamp", new Timestamp(2021 - 1900, 3 - 1, 28, 4, 0, 0, 0), "2021-03-28 02:00:00");

    restartServer("GMT", "Europe/Berlin"); 
    doTest("propTimestamp", new Timestamp(2021 - 1900, 11 - 1, 21, 4, 15, 15, 0), "2021-11-21 05:15:15");
    doTest("propTimestamp", new Timestamp(2021 - 1900, 8 - 1, 21, 3, 15, 15, 0), "2021-08-21 05:15:15"); // Check DST
    
    doTest("propTimestamp", new Timestamp(2021 - 1900, 3 - 1, 27, 1, 0, 0, 0), "2021-03-27 02:00:00");
    doTest("propTimestamp", new Timestamp(2021 - 1900, 3 - 1, 27, 23, 0, 0, 0), "2021-03-28 00:00:00");
    doTest("propTimestamp", new Timestamp(2021 - 1900, 3 - 1, 28, 0, 0, 0, 0), "2021-03-28 01:00:00");
    doTest("propTimestamp", new Timestamp(2021 - 1900, 3 - 1, 28, 1, 0, 0, 0), "2021-03-28 03:00:00");
  }

  @Test
  public void testUtilDate() {
    restartServer("PST", "PST"); // java & db in same TZ
    doTest("utilDate", new java.util.Date(2021 - 1900, 11 - 1, 21, 5, 15, 15), "2021-11-21 05:15:15");
    if (config.getJsonDateTime() == io.ebean.config.JsonConfig.DateTime.ISO8601) {
      softly.assertThat(json).isEqualTo("{\"utilDate\":\"2021-11-21T13:15:15.000Z\"}");
    } else if (config.getJsonDateTime()  == io.ebean.config.JsonConfig.DateTime.MILLIS) {
      softly.assertThat(json).isEqualTo("{\"utilDate\":1637500515000}");
    } else {
      // softly.assertThat(json).isEqualTo("{\"utilDate\":1637500515.000000000}");
      softly.assertThat(json).isEqualTo("{\"utilDate\":1637500515000}"); // FIXME: util date uses millis here
    }

    
    restartServer("Europe/Berlin", "GMT"); // go to germany
    doTest("utilDate", new java.util.Date(2021 - 1900, 11 - 1, 21, 6, 15, 15), "2021-11-21 05:15:15");
    doTest("utilDate", new java.util.Date(2021 - 1900, 8 - 1, 21, 7, 15, 15), "2021-08-21 05:15:15");
    
    doTest("utilDate", new java.util.Date(2021 - 1900, 3 - 1, 27, 23, 0, 0), "2021-03-27 22:00:00");
    doTest("utilDate", new java.util.Date(2021 - 1900, 3 - 1, 28, 0, 0, 0), "2021-03-27 23:00:00");
    doTest("utilDate", new java.util.Date(2021 - 1900, 3 - 1, 28, 1, 0, 0), "2021-03-28 00:00:00");
    doTest("utilDate", new java.util.Date(2021 - 1900, 3 - 1, 28, 2, 0, 0), "2021-03-28 01:00:00");
    doTest("utilDate", new java.util.Date(2021 - 1900, 3 - 1, 28, 3, 0, 1), "2021-03-28 01:00:01");
    doTest("utilDate", new java.util.Date(2021 - 1900, 3 - 1, 28, 4, 0, 0), "2021-03-28 02:00:00");

    restartServer("GMT", "Europe/Berlin"); 
    doTest("utilDate", new java.util.Date(2021 - 1900, 11 - 1, 21, 4, 15, 15), "2021-11-21 05:15:15");
    doTest("utilDate", new java.util.Date(2021 - 1900, 8 - 1, 21, 3, 15, 15), "2021-08-21 05:15:15");
    
    doTest("utilDate", new java.util.Date(2021 - 1900, 3 - 1, 27, 1, 0, 0), "2021-03-27 02:00:00");
    doTest("utilDate", new java.util.Date(2021 - 1900, 3 - 1, 27, 23, 0, 0), "2021-03-28 00:00:00");
    doTest("utilDate", new java.util.Date(2021 - 1900, 3 - 1, 28, 0, 0, 0), "2021-03-28 01:00:00");
    doTest("utilDate", new java.util.Date(2021 - 1900, 3 - 1, 28, 1, 0, 0), "2021-03-28 03:00:00");
  }
  
  @Test
  public void testOffsetDateTime() {

    restartServer("PST", "PST"); // be in the same TZ
    doTest("offsetDateTime", OffsetDateTime.parse("2021-11-20T21:15:15-08:00"), "2021-11-20 21:15:15");
    if (config.getJsonDateTime() == io.ebean.config.JsonConfig.DateTime.ISO8601) {
      softly.assertThat(json).isEqualTo("{\"offsetDateTime\":\"2021-11-21T05:15:15Z\"}");
    } else if (config.getJsonDateTime()  == io.ebean.config.JsonConfig.DateTime.MILLIS) {
      softly.assertThat(json).isEqualTo("{\"offsetDateTime\":1637471715000}");
    } else {
      softly.assertThat(json).isEqualTo("{\"offsetDateTime\":1637471715.000000000}");
    }
    
    restartServer("PST", "GMT"); // pass PST offsetDateTime to GMT DB
    doTest("offsetDateTime", OffsetDateTime.parse("2021-11-20T21:15:15-08:00"), "2021-11-21 05:15:15");
    
  }

  @Test
  public void testZonedDateTime() {

    restartServer("PST", "PST"); // be in the same TZ
    doTest("zonedDateTime", ZonedDateTime.parse("2021-11-20T21:15:15-08:00"), "2021-11-20 21:15:15");
    if (config.getJsonDateTime() == io.ebean.config.JsonConfig.DateTime.ISO8601) {
      softly.assertThat(json).isEqualTo("{\"zonedDateTime\":\"2021-11-21T05:15:15Z\"}");
    } else if (config.getJsonDateTime()  == io.ebean.config.JsonConfig.DateTime.MILLIS) {
      softly.assertThat(json).isEqualTo("{\"zonedDateTime\":1637471715000}"); // 05:15 GMT
    } else {
      softly.assertThat(json).isEqualTo("{\"zonedDateTime\":1637471715.000000000}");
    }
    
    restartServer("PST", "GMT"); // pass PST offsetDateTime to GMT DB
    doTest("zonedDateTime", ZonedDateTime.parse("2021-11-20T21:15:15-08:00"), "2021-11-21 05:15:15");
    
  }
//  offsetDateTime : OffsetDateTime
//  zonedDateTime : ZonedDateTime
  private String simpleClassNameOf(StackTraceElement testStackTraceElement) {
    String className = testStackTraceElement.getClassName();
    return className.substring(className.lastIndexOf('.') + 1);
  }

  private <T extends Comparable<? super T>> void doTest(String property, T javaValue, String sqlValue) {
    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    String testClassName = simpleClassNameOf(stackTrace[2]);
    String testName = stackTrace[2].getMethodName();
    int lineNumber = stackTrace[2].getLineNumber();
    String testLoc = format("at %s.%s(%s.java:%s)", testClassName, testName, testClassName, lineNumber);

    db.find(MDateTime.class).delete(); // clear database
    String sqlColumn = CamelCaseHelper.toUnderscoreFromCamel(property);
    // insert with raw sql
    db.sqlUpdate("insert into mdate_time (id, " + sqlColumn + ") values (1, '" + sqlValue + "')").execute();

    // check findSingleAttributeList
    List<T> list = db.find(MDateTime.class).select(property).findSingleAttributeList();
    softly.assertThat(list).as("find " + testLoc).hasSize(1);
    T attr = list.get(0);
    assertTimeEquals("find attribute " + testLoc, attr, javaValue);

    // check find model
    MDateTime model = db.find(MDateTime.class).where().eq(property, javaValue).findOne();
    Property beanProp = db.pluginApi().beanType(MDateTime.class).property(property);
    softly.assertThat(model).as("find model " + testLoc).isNotNull();
    if (model != null) {
      @SuppressWarnings("unchecked")
      T beanValue = (T) beanProp.value(model);
      assertTimeEquals("read from model " + testLoc, beanValue, javaValue);
    }
    // insert with "save"
    model = new MDateTime();
    model.setId(2);
    ((ExpressionPath) beanProp).pathSet(model, javaValue);
    db.save(model);

    // check findCount
    int count = db.find(MDateTime.class).where().eq(property, javaValue).findCount();
    softly.assertThat(count).as("find count " + testLoc).isEqualTo(2);

    JsonWriteOptions opts = new JsonWriteOptions();
    opts.setPathProperties(PathProperties.parse(property));
    // check json roundtrip
    json = db.json().toJson(model, opts);
    model = db.json().toBean(MDateTime.class, json);
    T beanValue = (T) beanProp.value(model);
    assertTimeEquals("json roundtrip " + testLoc, beanValue, javaValue);

    ScalarType<T> st = (ScalarType) ((BeanProperty) beanProp).scalarType();
    formatted = st.formatValue(javaValue);
    assertTimeEquals("parse/format symmetry " + testLoc, st.parse(formatted), javaValue);
    if (st instanceof ScalarTypeBaseDate) {
      ScalarTypeBaseDate<T> st2 = (ScalarTypeBaseDate<T>) st;
      Date date = st2.convertToDate(javaValue);
      assertTimeEquals("date convert symmetry " + testLoc, st2.convertFromDate(date), javaValue);
      millis = st2.convertToMillis(javaValue);
      assertTimeEquals("millis convert symmetry " + testLoc, st2.convertFromMillis(millis), javaValue);
    }
    if (st instanceof ScalarTypeBaseDateTime) {
      ScalarTypeBaseDateTime<T> st2 = (ScalarTypeBaseDateTime<T>) st;
      Timestamp ts = st2.convertToTimestamp(javaValue);
      assertTimeEquals("timestamp convert symmetry " + testLoc, st2.convertFromTimestamp(ts), javaValue);
       millis = st2.convertToMillis(javaValue);
      assertTimeEquals("millis convert symmetry " + testLoc, st2.convertFromMillis(millis), javaValue);
      assertTimeEquals("instant convert symmetry " + testLoc, st2.convertFromInstant(Instant.ofEpochMilli(millis)),
          javaValue);
    }
  }

  private <T extends Comparable<? super T>> void assertTimeEquals(String msg, T value, T expected) {
    if (value instanceof java.sql.Date) {
      // SQL Dates may not be aligned at 00:00 - so compare toString representation
      softly.assertThat(value.toString()).as(msg).isEqualTo(expected.toString());
    } else if (value instanceof OffsetDateTime) {
      softly.assertThat((OffsetDateTime) value).as(msg).isAtSameInstantAs((OffsetDateTime) expected);
    } else if (value instanceof ZonedDateTime) {
      softly.assertThat(((ZonedDateTime) value).toInstant()).as(msg).isEqualTo(((ZonedDateTime) expected).toInstant());
    } else {
      softly.assertThat(value).as(msg).isEqualByComparingTo(expected);
    }
  }

}
