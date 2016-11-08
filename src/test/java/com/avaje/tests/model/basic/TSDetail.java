package com.avaje.tests.model.basic;

import javax.persistence.*;

/**
 * A basic entity to test simple things.
 */
@Entity
@Table(name = "t_detail_with_other_namexxxyy")
public class TSDetail {


  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "t_atable_detail_seq")
  @Id
  Integer id;

  String name;

  String description;

  boolean active;

  @ManyToOne
  TSMaster master;

  public TSDetail(String name) {
    this.name = name;
  }

  public TSDetail() {

  }

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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public TSMaster getMaster() {
    return master;
  }

  public void setMaster(TSMaster master) {
    this.master = master;
  }
}
