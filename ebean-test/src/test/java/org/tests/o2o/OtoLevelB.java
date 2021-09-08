package org.tests.o2o;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class OtoLevelB {

  @Id
  private Long id;

  private final String name;

  @OneToOne(cascade = CascadeType.ALL)
  private OtoLevelC c;

  public OtoLevelB(String name) {
    this.name = name;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public OtoLevelC getC() {
    return c;
  }

  public void setC(OtoLevelC c) {
    this.c = c;
  }
}
