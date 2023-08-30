package org.tests.inheritance.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class Configurations {
  @Id
  @Column(name = "id")
  private Integer id;

  private String name;

  @OneToMany
  private List<Configuration> groupConfigurations;

  @OneToMany
  private List<Configuration> productConfigurations;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Configuration> getGroupConfigurations() {
    return groupConfigurations;
  }

  public void setGroupConfigurations(List<Configuration> groupConfigurations) {
    this.groupConfigurations = groupConfigurations;
  }

  public void addGroupConfiguration(Configuration groupConfiguration) {
    groupConfiguration.setConfigurations(this);
    groupConfigurations.add(groupConfiguration);
  }

  public List<Configuration> getProductConfigurations() {
    return productConfigurations;
  }

  public void setProductConfigurations(List<Configuration> productConfigurations) {
    this.productConfigurations = productConfigurations;
  }

  public void addProductConfiguration(Configuration productConfiguration) {
    productConfiguration.setConfigurations(this);
    productConfigurations.add(productConfiguration);
  }
}
