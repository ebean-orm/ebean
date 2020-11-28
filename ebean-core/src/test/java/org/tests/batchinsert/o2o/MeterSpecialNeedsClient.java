package org.tests.batchinsert.o2o;

import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

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
