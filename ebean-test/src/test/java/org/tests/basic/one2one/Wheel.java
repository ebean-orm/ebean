package org.tests.basic.one2one;

import javax.persistence.*;

@Entity
@Table(name = "wheel")
public class Wheel {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  private Long id;

  @Version
  private int version;

  @OneToOne(mappedBy = "wheel", cascade = CascadeType.PERSIST)
  private Tire tire;

  public Long getId() {
    return id;
  }

  public Wheel() {
    super();
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

  public Tire getTire() {
    return tire;
  }

  public void setTire(Tire tire) {
    this.tire = tire;
  }
}
