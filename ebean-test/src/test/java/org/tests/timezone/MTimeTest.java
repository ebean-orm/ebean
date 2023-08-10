package org.tests.timezone;

import javax.annotation.Nullable;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Test model with various time types.
 */
@Entity
public class MTimeTest {

  @Id
  private Integer id;
  @Nullable
  private Instant instant;
  @Nullable
  private LocalDate localDate;
  @Nullable
  private LocalTime localTime;
  @Nullable
  private LocalDateTime localDateTime;
  @Nullable
  private Timestamp timestamp;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  @Nullable
  public Instant getInstant() {
    return instant;
  }

  public void setInstant(@Nullable Instant instant) {
    this.instant = instant;
  }

  @Nullable
  public LocalDate getLocalDate() {
    return localDate;
  }

  public void setLocalDate(@Nullable LocalDate localDate) {
    this.localDate = localDate;
  }

  @Nullable
  public LocalTime getLocalTime() {
    return localTime;
  }

  public void setLocalTime(@Nullable LocalTime localTime) {
    this.localTime = localTime;
  }

  @Nullable
  public LocalDateTime getLocalDateTime() {
    return localDateTime;
  }

  public void setLocalDateTime(@Nullable LocalDateTime localDateTime) {
    this.localDateTime = localDateTime;
  }

  @Nullable
  public Timestamp getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(@Nullable Timestamp timestamp) {
    this.timestamp = timestamp;
  }
}
