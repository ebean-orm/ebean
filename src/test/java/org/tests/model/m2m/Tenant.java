package org.tests.model.m2m;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;
import java.util.Set;
import java.util.UUID;

/**
 * The Class Tenant.
 */
@Entity
@Table(name = "mt_tenant")
public class Tenant {

  @Id
  private UUID id;

  @Column
  private String name;

  @OneToMany(mappedBy = "tenant", cascade = CascadeType.REMOVE)
  private Set<Role> roles;

  @Version
  private Long version;

  public Tenant(String name) {
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

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  public Set<Role> getRoles() {
    return roles;
  }

  public void setRoles(Set<Role> roles) {
    this.roles = roles;
  }

  @Override
  public String toString() {
    return "name:" + name + " id:" + id;
  }
}
