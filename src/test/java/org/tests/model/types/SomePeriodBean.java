package org.tests.model.types;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import java.time.MonthDay;

@Entity
public class SomePeriodBean {

  @Id
  Long id;

  @Version
  Long version;

  MonthDay anniversary;

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

  public MonthDay getAnniversary() {
    return anniversary;
  }

  public void setAnniversary(MonthDay anniversary) {
    this.anniversary = anniversary;
  }
}
