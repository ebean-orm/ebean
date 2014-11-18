package com.avaje.tests.model.types;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import java.time.*;

@Entity
public class SomePeriodBean {

  @Id
  Long id;

  @Version
  Long version;

  Period period;

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

  public Period getPeriod() {
    return period;
  }

  public void setPeriod(Period period) {
    this.period = period;
  }
}
