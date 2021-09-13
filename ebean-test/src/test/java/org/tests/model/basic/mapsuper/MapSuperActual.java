package org.tests.model.basic.mapsuper;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class MapSuperActual extends MapSuperNoId {

  @Id
  Long id;

  String name;

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

}
