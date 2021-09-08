package org.tests.model.carwheel;

import io.ebean.annotation.Aggregation;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;
import java.util.List;

@Entity
@Table(name = "sa_car")
public class Car {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  protected Long id;

  @Version
  private int version;

  private String brand;

  private int sold;

  @Aggregation("sum(sold)")
  private int totalSold;

  @OneToMany(mappedBy = "car", cascade = CascadeType.ALL)
  private List<Wheel> wheels;

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

  public String getBrand() {
    return brand;
  }

  public void setBrand(String brand) {
    this.brand = brand;
  }

  public int getSold() {
    return sold;
  }

  public void setSold(int sold) {
    this.sold = sold;
  }

  public int getTotalSold() {
    return totalSold;
  }

  public void setTotalSold(int totalSold) {
    this.totalSold = totalSold;
  }

  public List<Wheel> getWheels() {
    return wheels;
  }

  public void setWheels(List<Wheel> wheels) {
    this.wheels = wheels;
  }

}
