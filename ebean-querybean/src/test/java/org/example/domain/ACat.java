package org.example.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;

@Inheritance
@DiscriminatorValue("CAT")
@Entity
public class ACat extends Animal {

  public ACat(String name) {
    super(name);
  }

}
