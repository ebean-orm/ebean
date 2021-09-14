package org.tests.aggregateformula;

import javax.persistence.*;

@Entity
@Inheritance
@Table(name = "iaf_segment")
@DiscriminatorColumn(name = "ptype")
public class IAFBaseSegment {

  @Id
  private Long id;

}
