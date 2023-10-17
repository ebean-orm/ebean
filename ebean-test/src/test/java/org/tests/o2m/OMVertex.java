package org.tests.o2m;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.List;
import java.util.UUID;

@Entity
public class OMVertex {

  @Id
  private UUID id;

  @OneToMany(cascade = CascadeType.ALL)
  private List<OMVertexOther> related;

  public OMVertex(UUID id) {
    this.id = id;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public List<OMVertexOther> getRelated() {
    return related;
  }

  public void setRelated(List<OMVertexOther> related) {
    this.related = related;
  }
}
