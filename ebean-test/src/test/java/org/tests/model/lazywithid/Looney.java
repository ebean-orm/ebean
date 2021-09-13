package org.tests.model.lazywithid;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Looney {
  @Id
  public Long id;

  @ManyToOne
  private Tune tune;

  private String name;

  public Looney(final String name) {
    this.name = name;
  }

  public Tune getTune() {
    return tune;
  }

  public void setTune(final Tune tune) {
    this.tune = tune;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }
}
