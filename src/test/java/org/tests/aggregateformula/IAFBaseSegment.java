package org.tests.aggregateformula;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.Table;

@Entity
@Inheritance
@Table(name = "iaf_segment")
@DiscriminatorColumn(name = "ptype")
public class IAFBaseSegment {

  @Id
  private Long id;

}
