package org.tests.o2o;


import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class OtoLevelA {

  @Id
  private Long id;

  private final String name;

  @OneToOne(cascade = CascadeType.ALL)
  private OtoLevelB b;

  public OtoLevelA(String name) {
    this.name = name;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public OtoLevelB getB() {
    return b;
  }

  public void setB(OtoLevelB b) {
    this.b = b;
  }
}
