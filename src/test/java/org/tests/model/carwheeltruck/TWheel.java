package org.tests.model.carwheeltruck;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class TWheel {

  @Id
  Long id;

  @ManyToOne(optional = false)
  TCar owner;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public TCar getOwner() {
    return owner;
  }

  public void setOwner(TCar owner) {
    this.owner = owner;
  }

}
