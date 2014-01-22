package com.avaje.tests.model.map;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.*;

@Entity
public class MpUser {

  @Id
  private Long id;

  private String name;

  @OneToMany(cascade = CascadeType.ALL)
  @MapKey(name = "id")
  public Map<Long, MpRole> roles = new HashMap<Long, MpRole>();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Map<Long, MpRole> getRoles() {
    return roles;
  }

  public void setRoles(Map<Long, MpRole> roles) {
    this.roles = roles;
  }
}
