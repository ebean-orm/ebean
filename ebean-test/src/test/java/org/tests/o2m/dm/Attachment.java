package org.tests.o2m.dm;

import javax.persistence.Entity;

@Entity
public class Attachment extends HistoryColumns {
  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
