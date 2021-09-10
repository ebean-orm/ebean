package org.tests.model.carwheel;

import javax.persistence.*;

@Entity
@Table(name = "sa_wheel")
public class Wheel {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  protected Long id;

  @Version
  private int version;

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "tire")
  private Tire tire;

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "car")
  private Car car;

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

  public Tire getTire() {
    return tire;
  }

  public void setTire(Tire tire) {
    this.tire = tire;
  }

  public Car getCar() {
    return car;
  }

  public void setCar(Car car) {
    this.car = car;
  }

}
