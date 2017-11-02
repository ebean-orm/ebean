package org.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import java.io.Serializable;

@Entity
public class GraphEdge implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  Integer id;

  // note: property is a reserved sql keyword. Resulting column: from_id
  @ManyToOne
  GraphNode from;

  @ManyToOne
  GraphNode to;

}
