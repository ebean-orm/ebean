package org.tests.model.basic;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

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
