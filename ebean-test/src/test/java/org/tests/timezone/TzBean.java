package org.tests.timezone;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "tz_bean")
public class TzBean {

  @Id
  Long id;

  @Column(name = "moda")
  String mode;

  Timestamp ts;
  Timestamp tstz;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public Timestamp getTs() {
    return ts;
  }

  public void setTs(Timestamp ts) {
    this.ts = ts;
  }

  public Timestamp getTstz() {
    return tstz;
  }

  public void setTstz(Timestamp tstz) {
    this.tstz = tstz;
  }
}
