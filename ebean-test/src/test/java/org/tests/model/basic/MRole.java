package org.tests.model.basic;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "mrole")
public class MRole {

  @Id
  @Column(name = "role_id")
  Integer roleId;

  String roleName;

  @ManyToMany(cascade = CascadeType.ALL)
  List<MUser> users;

  public MRole() {

  }

  public MRole(String roleName) {
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

  public List<MUser> getUsers() {
    return users;
  }

  public void setUsers(List<MUser> users) {
    this.users = users;
  }

  @Override
  public String toString() {
    return "MRole [roleName=" + roleName + "]";
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }

    // Make sure other is not null and has the same class as this
    if (other != null && getClass().equals(other.getClass())) {
      final MRole rhs = (MRole) other;
      if (roleId.equals(rhs.roleId)) {
        if (roleId == 0) {
          return false;
        } else {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    if (roleId != null && roleId != 0) {
      int rid = roleId;
      return (int) (rid ^ (rid >>> 32));
    }
    return super.hashCode();
  }

}
