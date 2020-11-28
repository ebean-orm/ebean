package org.example.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;

@Inheritance
@DiscriminatorValue("WC")
@Entity
public class AWildCat extends ACat {

  public AWildCat(String name) {
    super(name);
  }

}
