package org.tests.batchinsert.o2o;

import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class MeterSpecialNeedsContact {
  @Id
  private UUID id;

  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
