package org.tests.model.basic;

import io.ebean.annotation.Formula;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("CAT")
public class Cat extends Animal {

  String name;

  @Formula(select = "${ta}.species")
  String catFormula;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCatFormula() {
    return catFormula;
  }

  public void setCatFormula(String catFormula) {
    this.catFormula = catFormula;
  }
}
