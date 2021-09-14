package org.tests.o2m;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity
public class OMVertexOther {

  @Id
  private UUID id;

  private final String name;

  public OMVertexOther(String name) {
    this.name = name;
  }
}
