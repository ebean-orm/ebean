package org.tests.inheritance.model;

import io.ebean.annotation.Cache;
import io.ebean.annotation.ChangeLog;

import jakarta.persistence.*;
import java.util.List;

@ChangeLog
@Entity
@Cache(enableQueryCache = true)
public class Configuration extends AbstractBaseClass {

  @Id
  @Column(name = "id")
  private Integer id;


  @ManyToOne
  private Configurations configurations;

  private String groupName;

  @OneToMany(mappedBy = "groupConfiguration")
  private List<CalculationResult> groupResults;

  private String productName;

  @OneToMany(mappedBy = "productConfiguration")
  private List<CalculationResult> productResults;

  public Configuration() {
    super();
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Configurations getConfigurations() {
    return configurations;
  }

  public void setConfigurations(Configurations configurations) {
    this.configurations = configurations;
  }

  public String getProductName() {
    return productName;
  }

  public String getGroupName() {
    return groupName;
  }
}
