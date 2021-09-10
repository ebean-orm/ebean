package org.tests.model.types;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import java.nio.file.Path;
import java.sql.Date;
import java.time.*;

@Entity
public class SomeNewTypesBean {

  @Id
  Long id;

  @Version
  Long version;

  @Column(name = "dow")
  DayOfWeek dayOfWeek;

  @Column(name = "mth")
  Month month;

  @Column(name = "yr")
  Year year;

  @Column(name = "yr_mth")
  YearMonth yearMonth;

  @Column(name = "month_day")
  MonthDay monthDay;

  java.sql.Date sqlDate;

  java.sql.Time sqlTime;

  LocalDate localDate;

  LocalDateTime localDateTime;

  OffsetDateTime offsetDateTime;

  ZonedDateTime zonedDateTime;

  LocalTime localTime;

  Instant instant;

  ZoneId zoneId;

  ZoneOffset zoneOffset;

  Path path;

  Period period;

  Duration duration;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  public DayOfWeek getDayOfWeek() {
    return dayOfWeek;
  }

  public void setDayOfWeek(DayOfWeek dayOfWeek) {
    this.dayOfWeek = dayOfWeek;
  }

  public Month getMonth() {
    return month;
  }

  public void setMonth(Month month) {
    this.month = month;
  }

  public Year getYear() {
    return year;
  }

  public void setYear(Year year) {
    this.year = year;
  }

  public YearMonth getYearMonth() {
    return yearMonth;
  }

  public void setYearMonth(YearMonth yearMonth) {
    this.yearMonth = yearMonth;
  }

  public MonthDay getMonthDay() {
    return monthDay;
  }

  public void setMonthDay(MonthDay monthDay) {
    this.monthDay = monthDay;
  }

  public Date getSqlDate() {
    return sqlDate;
  }

  public void setSqlDate(Date sqlDate) {
    this.sqlDate = sqlDate;
  }

  public java.sql.Time getSqlTime() {
    return sqlTime;
  }

  public void setSqlTime(java.sql.Time sqlTime) {
    this.sqlTime = sqlTime;
  }

  public LocalDate getLocalDate() {
    return localDate;
  }

  public void setLocalDate(LocalDate localDate) {
    this.localDate = localDate;
  }

  public LocalDateTime getLocalDateTime() {
    return localDateTime;
  }

  public void setLocalDateTime(LocalDateTime localDateTime) {
    this.localDateTime = localDateTime;
  }

  public OffsetDateTime getOffsetDateTime() {
    return offsetDateTime;
  }

  public void setOffsetDateTime(OffsetDateTime offsetDateTime) {
    this.offsetDateTime = offsetDateTime;
  }

  public ZonedDateTime getZonedDateTime() {
    return zonedDateTime;
  }

  public void setZonedDateTime(ZonedDateTime zonedDateTime) {
    this.zonedDateTime = zonedDateTime;
  }

  public LocalTime getLocalTime() {
    return localTime;
  }

  public void setLocalTime(LocalTime localTime) {
    this.localTime = localTime;
  }

  public Instant getInstant() {
    return instant;
  }

  public void setInstant(Instant instant) {
    this.instant = instant;
  }

  public ZoneId getZoneId() {
    return zoneId;
  }

  public void setZoneId(ZoneId zoneId) {
    this.zoneId = zoneId;
  }

  public ZoneOffset getZoneOffset() {
    return zoneOffset;
  }

  public void setZoneOffset(ZoneOffset zoneOffset) {
    this.zoneOffset = zoneOffset;
  }

  public Path getPath() {
    return path;
  }

  public void setPath(Path path) {
    this.path = path;
  }

  public Period getPeriod() {
    return period;
  }

  public void setPeriod(Period period) {
    this.period = period;
  }

  public Duration getDuration() {
    return duration;
  }

  public void setDuration(Duration duration) {
    this.duration = duration;
  }
}
