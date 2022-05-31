package org.tests.timezone;

import javax.annotation.Nullable;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDate;

@Entity
public class MLocalDate {

  @Id
  private Integer id;

  @Nullable
  private LocalDate localDate;

  @Nullable
  public LocalDate getLocalDate() {
    return localDate;
  }
  public Integer getId() {
    return id;
  }
  public void setId(Integer id) {
    this.id = id;
  }

}
