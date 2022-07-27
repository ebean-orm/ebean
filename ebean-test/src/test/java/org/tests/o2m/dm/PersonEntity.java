package org.tests.o2m.dm;

import org.tests.model.draftable.BaseDomain;

import javax.persistence.Entity;

@Entity
public class PersonEntity extends BaseDomain {

  public PersonEntity() {
  }

  public PersonEntity(Long id) {
    this.setId(id);
  }

  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
