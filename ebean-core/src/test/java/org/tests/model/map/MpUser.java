package org.tests.model.map;

import javax.persistence.*;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
public class MpUser {

  @Id
  private Long id;

  private String name;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @MapKey(name = "code")
  private Map<String, MpRole> roles = new LinkedHashMap<>();

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

  public Map<String, MpRole> getRoles() {
    return roles;
  }

  public void setRoles(Map<String, MpRole> roles) {
    this.roles = roles;
  }
}
