package org.tests.model.basic;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class TJodaEntity {

  @Id
  Integer id;

  LocalTime localTime;
  LocalDate localDate;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public LocalTime getLocalTime() {
    return localTime;
  }

  public void setLocalTime(LocalTime localTime) {
    this.localTime = localTime;
  }

  public LocalDate getLocalDate() {
    return localDate;
  }

  public TJodaEntity setLocalDate(LocalDate localDate) {
    this.localDate = localDate;
    return this;
  }
}
