package org.tests.iud;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;

import static jakarta.persistence.CascadeType.ALL;

@Entity
public class PcfCity extends PcfModel {

  private final String name;

  @OneToOne(cascade = ALL, optional = false)
  private PcfPerson mayor;

  @OneToOne(cascade = ALL, optional = false)
  private PcfPerson viceMayor;

  public PcfCity(String name, PcfPerson mayor, PcfPerson viceMayor) {
    this.name = name;
    this.mayor = mayor;
    this.viceMayor = viceMayor;
  }
}
