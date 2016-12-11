package org.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.TimeZone;

@Entity
public class BeanWithTimeZone {

  @Id
  Long id;

  String name;

  TimeZone timezone;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public TimeZone getTimezone() {
    return timezone;
  }

  public void setTimezone(TimeZone timezone) {
    this.timezone = timezone;
  }

}
