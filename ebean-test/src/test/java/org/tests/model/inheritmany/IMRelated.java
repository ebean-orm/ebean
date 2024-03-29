package org.tests.model.inheritmany;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class IMRelated {

  @Id
  Long id;

  String name;

  @ManyToOne(optional = false)
  IMRoot owner;

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

  public IMRoot getOwner() {
    return owner;
  }

  public void setOwner(IMRoot owner) {
    this.owner = owner;
  }

}
