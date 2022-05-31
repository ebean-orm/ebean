package org.tests.timezone;

import javax.annotation.Nullable;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class MLocalDateTime {

  @Id
  private Integer id;

  @Nullable
  private LocalDateTime localDateTime;

  @Nullable
  public LocalDateTime getLocalDateTime() {
    return localDateTime;
  }
  public Integer getId() {
    return id;
  }
  public void setId(Integer id) {
    this.id = id;
  }

}
