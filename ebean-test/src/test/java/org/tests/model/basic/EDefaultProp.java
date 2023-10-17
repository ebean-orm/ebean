package org.tests.model.basic;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class EDefaultProp {

  @Id
  Integer id;

  @OneToOne(cascade = CascadeType.ALL)
  ESimple eSimple;

  String name;

  public EDefaultProp() {
    eSimple = new ESimple();
    eSimple.setName("Default prop eSimple");
    name = "defaultName";
  }

  public Integer getId() {
    return id;
  }

  public void setId(final Integer id) {
    this.id = id;
  }

  public ESimple geteSimple() {
    return eSimple;
  }

  public void seteSimple(final ESimple eSimple) {
    this.eSimple = eSimple;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }
}
