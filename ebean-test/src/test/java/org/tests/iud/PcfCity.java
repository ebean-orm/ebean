package org.tests.iud;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import static javax.persistence.CascadeType.ALL;

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
