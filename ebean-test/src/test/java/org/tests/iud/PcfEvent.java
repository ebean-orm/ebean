package org.tests.iud;

import jakarta.persistence.Entity;

@Entity
public class PcfEvent extends PcfModel {

  final String name;

  public PcfEvent(String name) {
    this.name = name;
  }
}
