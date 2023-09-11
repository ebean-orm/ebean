package org.tests.model.bridge;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.util.UUID;

@Entity
public class BSite {

  @Id @GeneratedValue
  UUID id;

  String name;

  public BSite(String name) {
    this.name = name;
  }

}
