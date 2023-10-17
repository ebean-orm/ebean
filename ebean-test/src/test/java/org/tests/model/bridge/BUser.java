package org.tests.model.bridge;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.util.UUID;

@Entity
public class BUser {

  @Id
  UUID id;

  String name;

  public BUser(String name) {
    this.name = name;
  }

}
