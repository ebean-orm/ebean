package org.tests.timezone;

import javax.annotation.Nullable;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;

@Entity
public class MInstant {

  @Id
  private Integer id;

  @Nullable
  private Instant instant;

  @Nullable
  public Instant getInstant() {
    return instant;
  }
  public Integer getId() {
    return id;
  }
  public void setId(Integer id) {
    this.id = id;
  }

}
