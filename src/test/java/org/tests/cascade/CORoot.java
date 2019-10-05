package org.tests.cascade;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import static javax.persistence.CascadeType.ALL;

@Entity
public class CORoot {

  @Id
  private long id;

  private final String name;

  @OneToOne(cascade = ALL, orphanRemoval = true)
  private COOne one;

  public CORoot(String name, COOne one) {
    this.name = name;
    this.one = one;
  }

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public COOne getOne() {
    return one;
  }
}
