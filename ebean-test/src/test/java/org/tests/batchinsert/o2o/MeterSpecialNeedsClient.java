package org.tests.batchinsert.o2o;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import java.util.UUID;

@Entity
public class MeterSpecialNeedsClient {
  @Id
  private UUID id;

  private String name;

  @OneToOne(cascade = CascadeType.ALL)
  private MeterSpecialNeedsContact primary;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public MeterSpecialNeedsContact getPrimary() {
    return primary;
  }

  public void setPrimary(MeterSpecialNeedsContact primary) {
    this.primary = primary;
  }
}
