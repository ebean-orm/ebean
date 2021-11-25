package org.tests.model.basic;

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

import javax.annotation.Nullable;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.joda.time.DateTime;

@Entity
public class MDateTime {

  @Id
  private Integer id;

  @Nullable
  private LocalTime localTime;

  @Nullable
  private LocalDateTime localDateTime;

  @Nullable
  private LocalDate localDate;

  @Nullable
  private OffsetDateTime offsetDateTime;

  @Nullable
  private ZonedDateTime zonedDateTime;
  
  @Nullable
  private YearMonth propYearMonth;
  
  @Nullable
  private MonthDay propMonthDay;
  
  @Nullable
  private Year propYear;

  @Nullable
  private Instant propInstant;

  @Nullable
  private Calendar propCalendar;

  @Nullable
  private Timestamp propTimestamp;
  
  @Nullable
  private java.sql.Date sqlDate;

  @Nullable
  private java.sql.Time sqlTime;

  @Nullable
  private java.util.Date utilDate;

  @Nullable
  private org.joda.time.DateTime jodaDateTime;

  @Nullable
  private org.joda.time.LocalDateTime jodaLocalDateTime;

  @Nullable
  private org.joda.time.LocalDate jodaLocalDate;

  @Nullable
  private org.joda.time.LocalTime jodaLocalTime;

  @Nullable
  private org.joda.time.DateMidnight jodaDateMidnight;

  public Integer getId() {
    return id;
  }
  public void setId(Integer id) {
    this.id = id;
  }
}
