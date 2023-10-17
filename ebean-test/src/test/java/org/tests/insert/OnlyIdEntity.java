package org.tests.insert;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class OnlyIdEntity {

  @Id
  long id;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }
}
