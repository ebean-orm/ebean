package org.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * A basic entity to test simple things.
 */
@Entity
@Table(name = "ts_detail_two")
public class TSDetailTwo {

  @Id
  Integer id;

  String name;

  String description;

  boolean active;

  @ManyToOne
  TSMasterTwo master;

  public TSDetailTwo(String name) {
    this.name = name;
  }

  public TSDetailTwo() {

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

  public TSMasterTwo getMaster() {
    return master;
  }

  public void setMaster(TSMasterTwo master) {
    this.master = master;
  }
}
