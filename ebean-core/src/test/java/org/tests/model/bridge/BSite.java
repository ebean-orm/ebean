package org.tests.model.bridge;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
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
