package org.tests.model.inheritmany;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class MProfile extends MBase {

  @ManyToOne(cascade = CascadeType.ALL)
  MPicture picture;

  String name;

  public MPicture getPicture() {
    return picture;
  }

  public void setPicture(MPicture picture) {
    this.picture = picture;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
