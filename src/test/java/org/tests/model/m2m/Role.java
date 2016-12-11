package org.tests.model.m2m;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;
import java.util.Set;
import java.util.UUID;

/**
 * The Class Role.
 */
@Entity
@Table(name = "mt_role")
public class Role {

  @Id
  private UUID id;

  @Column(length = 50)
  private String name;

  @ManyToMany(cascade = CascadeType.REMOVE)
  private Set<Permission> permissions;

  @ManyToOne
  private Tenant tenant;

  @Version
  private Long version;

  public Role(String name) {
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

  public Set<Permission> getPermissions() {
    return permissions;
  }

  public void setPermissions(Set<Permission> permissions) {
    this.permissions = permissions;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  public Tenant getTenant() {
    return tenant;
  }

  public void setTenant(Tenant tenant) {
    this.tenant = tenant;
  }

  @Override
  public String toString() {
    return "name:" + name + " id:" + id + " tenant:" + tenant;
  }
}
