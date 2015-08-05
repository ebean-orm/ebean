package com.avaje.tests.inheritance.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class Configurations {
  @Id
  @Column(name = "id")
  private Integer id;

  private String name;

  @OneToMany
  private List<GroupConfiguration> groupConfigurations;


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

  public List<GroupConfiguration> getGroupConfigurations() {
    return groupConfigurations;
  }


  public void setGroupConfigurations(List<GroupConfiguration> groupConfigurations) {
    this.groupConfigurations = groupConfigurations;
  }

  public void add(GroupConfiguration groupConfiguration) {
    groupConfiguration.setConfigurations(this);
    groupConfigurations.add(groupConfiguration);
  }
}
