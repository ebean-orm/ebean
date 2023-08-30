package org.tests.inheritance.model;

import javax.persistence.*;

@Entity
public class CalculationResult {

  @Id
  @Column(name = "id")
  private Integer id;

  private double charge;

  @ManyToOne(cascade = CascadeType.PERSIST)
  private Configuration productConfiguration;

  @ManyToOne(cascade = CascadeType.PERSIST)
  private Configuration groupConfiguration;

  public double getCharge() {
    return charge;
  }

  public void setCharge(double charge) {
    this.charge = charge;
  }

  public Configuration getProductConfiguration() {
    return productConfiguration;
  }

  public void setProductConfiguration(Configuration productConfiguration) {
    this.productConfiguration = productConfiguration;
  }

  public Configuration getGroupConfiguration() {
    return groupConfiguration;
  }

  public void setGroupConfiguration(Configuration groupConfiguration) {
    this.groupConfiguration = groupConfiguration;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }
}
