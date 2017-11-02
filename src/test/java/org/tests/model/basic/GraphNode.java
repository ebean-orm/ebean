package org.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import java.io.Serializable;
import java.util.List;

@Entity
public class GraphNode implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  Integer id;

  @OneToMany(mappedBy = "from")
  List<GraphEdge> out;
}
