package org.tests.aggregateformula;

import jakarta.persistence.*;

@MappedSuperclass
// DiscriminatorColumn(name = "ptype")
public abstract class IAFBaseSegment {

  @Id
  private Long id;

  String ptype;
}
