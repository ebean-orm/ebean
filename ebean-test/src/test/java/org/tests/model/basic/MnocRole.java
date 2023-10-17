package org.tests.model.basic;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
public class MnocRole {

  @Id
  Integer roleId;

  String roleName;

  @Version
  Integer version;

  /**
   * Only for testing.
   */
  public MnocRole(MnocRole other) {
    this.roleId = other.getRoleId();
    this.roleName = other.getRoleName();
    this.version = other.getVersion();
  }

  public MnocRole(String roleName) {
    this.roleName = roleName;
  }

  public Integer getRoleId() {
    return roleId;
  }

  public void setRoleId(Integer roleId) {
    this.roleId = roleId;
  }

  public String getRoleName() {
    return roleName;
  }

  public void setRoleName(String roleName) {
    this.roleName = roleName;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

}
