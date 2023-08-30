package org.tests.aggregateformula;

import javax.persistence.*;

@MappedSuperclass
// DiscriminatorColumn(name = "ptype")
public abstract class IAFBaseSegment {

  @Id
  private Long id;

  String ptype;
}
