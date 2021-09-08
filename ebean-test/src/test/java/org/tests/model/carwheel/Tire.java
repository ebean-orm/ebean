package org.tests.model.carwheel;

import javax.persistence.*;

@Entity
@Table(name = "sa_tire")
public class Tire {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  protected Long id;

  @Version
  private int version;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

}
