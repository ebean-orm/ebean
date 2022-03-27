package org.tests.model.basic;

import org.joda.time.LocalTime;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class TJodaEntity {

  @Id
  Integer id;

  LocalTime localTime;

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

}
