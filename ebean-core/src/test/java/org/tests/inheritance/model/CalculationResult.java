package org.tests.inheritance.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class CalculationResult {

  @Id
  @Column(name = "id")
  private Integer id;

  private double charge;

  @ManyToOne(cascade = CascadeType.PERSIST)
  private ProductConfiguration productConfiguration;

  @ManyToOne(cascade = CascadeType.PERSIST)
  private GroupConfiguration groupConfiguration;

  public double getCharge() {
    return charge;
  }

  public void setCharge(double charge) {
    this.charge = charge;
  }

  public ProductConfiguration getProductConfiguration() {
    return productConfiguration;
  }

  public void setProductConfiguration(ProductConfiguration productConfiguration) {
    this.productConfiguration = productConfiguration;
  }

  public GroupConfiguration getGroupConfiguration() {
    return groupConfiguration;
  }

  public void setGroupConfiguration(GroupConfiguration groupConfiguration) {
    this.groupConfiguration = groupConfiguration;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }
}
