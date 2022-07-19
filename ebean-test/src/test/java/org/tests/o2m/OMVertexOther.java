package org.tests.o2m;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity
public class OMVertexOther {

  @Id
  private UUID id;

  private final String name;
  private String other;

  public OMVertexOther(String name) {
    this.name = name;
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getOther() {
    return other;
  }

  public void setOther(String other) {
    this.other = other;
  }
}
