package org.example.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@DiscriminatorValue("SCAT")
@Entity
public class ASuperCat extends ACat {

  private String superCat;

  public ASuperCat(String name) {
    super(name);
  }

  public String getSuperCat() {
    return superCat;
  }

  public void setSuperCat(String superCat) {
    this.superCat = superCat;
  }
}
