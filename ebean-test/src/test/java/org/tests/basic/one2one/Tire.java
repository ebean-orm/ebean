package org.tests.basic.one2one;

import javax.persistence.*;

@Entity
@Table(name = "tire")
public class Tire {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  private Long id;
  @Version
  private int version;
  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "wheel")
  private Wheel wheel;

  public Tire() {
    super();
  }

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

  public Wheel getWheel() {
    return wheel;
  }

  public void setWheel(Wheel wheel) {
    this.wheel = wheel;
  }
}
