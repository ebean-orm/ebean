package org.tests.model.basic;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("BDG")
public class BigDog extends Dog {

  String dogSize;

  public String getDogSize() {
    return dogSize;
  }

  public void setDogSize(String dogSize) {
    this.dogSize = dogSize;
  }
}
