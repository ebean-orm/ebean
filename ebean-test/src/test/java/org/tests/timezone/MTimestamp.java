package org.tests.timezone;

import javax.annotation.Nullable;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;

@Entity
public class MTimestamp {

  @Id
  private Integer id;

  @Nullable
  private Timestamp timestamp;

  @Nullable
  public Timestamp getTimestamp() {
    return timestamp;
  }
  public Integer getId() {
    return id;
  }
  public void setId(Integer id) {
    this.id = id;
  }

}
