package org.example.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;

@Inheritance
@DiscriminatorValue("WC")
@Entity
public class AWildCat extends ACat {

  public AWildCat(String name) {
    super(name);
  }

}
