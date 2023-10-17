package org.example.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@DiscriminatorValue("CAT")
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
