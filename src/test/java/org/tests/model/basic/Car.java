package org.tests.model.basic;

import io.ebean.annotation.DbEnumValue;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import java.util.HashSet;
import java.util.Set;

@Entity
@Inheritance
@DiscriminatorValue("C")
public class Car extends Vehicle {
  private static final long serialVersionUID = -65427345082456523L;

  public enum Size {
    SMALL("S"),
    LARGE("L");

    String value;

    Size(String value) {
      this.value = value;
    }

    @DbEnumValue
    public String value() {
      return value;
    }
  }

  @Column(name = "siz")
  private Size size;

  private String driver;

  @ManyToOne
  private TruckRef carRef;

  @OneToMany(mappedBy = "car")
  @OrderBy("fuse.locationCode")
  private Set<CarAccessory> accessories = new HashSet<>();

  private String notes;

  public String getDriver() {
    return driver;
  }

  public void setDriver(String driver) {
    this.driver = driver;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public TruckRef getCarRef() {
    return carRef;
  }

  public void setCarRef(TruckRef carRef) {
    this.carRef = carRef;
  }

  public Set<CarAccessory> getAccessories() {
    return accessories;
  }

  public void setAccessories(Set<CarAccessory> accessories) {
    this.accessories = accessories;
  }

  public Size getSize() {
    return size;
  }

  public void setSize(Size size) {
    this.size = size;
  }
}
