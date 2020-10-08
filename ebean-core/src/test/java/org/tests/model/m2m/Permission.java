package org.tests.model.m2m;

import io.ebean.annotation.Cache;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.Set;
import java.util.UUID;

/**
 * The Class Permission.
 */
@Entity
@Cache(readOnly = true)
@Table(name = "mt_permission")
public class Permission {

  @Id
  private UUID id;

  @Column
  private String name;

  @ManyToMany(mappedBy = "permissions")
  private Set<Role> roles;

  public Permission(String name) {
    this.name = name;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Set<Role> getRoles() {
    return roles;
  }

  public void setRoles(Set<Role> roles) {
    this.roles = roles;
  }

  @Override
  public String toString() {
    return "name:" + name + "id:" + id;
  }

}
