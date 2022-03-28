package org.tests.model.bridge;

import javax.persistence.Entity;
import javax.persistence.Id;
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
