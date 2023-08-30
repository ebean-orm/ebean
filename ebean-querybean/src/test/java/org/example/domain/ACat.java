package org.example.domain;

import javax.persistence.Entity;

@Entity
public class ACat extends Animal {

  private String catProp;

  public ACat(String name) {
    super(name);
  }

  public String getCatProp() {
    return catProp;
  }

  public void setCatProp(String catProp) {
    this.catProp = catProp;
  }
}
