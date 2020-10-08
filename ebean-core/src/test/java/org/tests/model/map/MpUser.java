package org.tests.model.map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import java.util.HashMap;
import java.util.Map;

@Entity
public class MpUser {

  @Id
  private Long id;

  private String name;

  @OneToMany(cascade = CascadeType.ALL)
  @MapKey(name = "code")
  public Map<String, MpRole> roles = new HashMap<>();

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
