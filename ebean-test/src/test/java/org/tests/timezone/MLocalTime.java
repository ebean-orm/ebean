package org.tests.timezone;

import javax.annotation.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalTime;

@Entity
public class MLocalTime {

  @Id
  private Integer id;

  @Nullable
  private LocalTime localTime;

  @Nullable
  public LocalTime getLocalTime() {
    return localTime;
  }
  public Integer getId() {
    return id;
  }
  public void setId(Integer id) {
    this.id = id;
  }

}
