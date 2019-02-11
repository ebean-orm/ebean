package org.tests.sp.model.car;

import org.tests.sp.model.IdEntity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "sp_car_car")
public class Car extends IdEntity {

  private static final long serialVersionUID = 2579148859565507940L;

  private final String name;

  @ManyToMany(cascade = CascadeType.ALL)
  @JoinTable(name = "sp_car_car_wheels", joinColumns = {@JoinColumn(name = "car")}, inverseJoinColumns = {@JoinColumn(name = "wheel")})
  private List<Wheel> wheels;

  @ManyToMany(cascade = CascadeType.ALL)
  @JoinTable(name = "sp_car_car_doors", joinColumns = {@JoinColumn(name = "car")}, inverseJoinColumns = {@JoinColumn(name = "door")})
  private List<Door> doors;

  public Car(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public List<Wheel> getWheels() {
    return wheels;
  }

  public void setWheels(List<Wheel> wheels) {
    this.wheels = wheels;
  }

  public List<Door> getDoors() {
    return doors;
  }

  public void setDoors(List<Door> doors) {
    this.doors = doors;
  }
}
