package org.tests.iud;

import javax.persistence.Entity;

@Entity
public class PcfEvent extends PcfModel {

  final String name;

  public PcfEvent(String name) {
    this.name = name;
  }
}
