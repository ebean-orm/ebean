package org.tests.model.basic;

import io.ebean.annotation.PrivateOwned;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * A basic entity to test simple things.
 */
@Entity
@Table(name = "ts_master_two")
public class TSMasterTwo {

  @Id
  Integer id;

  String name;

  String description;

  boolean active;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "master")
  @PrivateOwned(cascadeRemove = false)
  List<TSDetailTwo> details;

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

  public List<TSDetailTwo> getDetails() {
    return details;
  }

  public void setDetails(List<TSDetailTwo> details) {
    this.details = details;
  }

  public void addDetail(TSDetailTwo detail) {
    if (details == null) {
      details = new ArrayList<>();
    }
    details.add(detail);
  }
}
