package org.tests.insert;

import javax.persistence.Entity;
import javax.persistence.Id;

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
