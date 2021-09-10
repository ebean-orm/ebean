package org.tests.cascade;

import javax.persistence.*;
import java.util.List;

@Entity
public class RelDetail {

  @Id
  Long id;

  String name;

  @Version
  int version;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "detail")
  private List<RelMaster> masterRel;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<RelMaster> getMasterRel() {
    return masterRel;
  }
}
