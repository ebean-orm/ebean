package org.tests.model.basic;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "mrole")
public class MRole {

  @Id
  Integer roleid;

  String roleName;

  @ManyToMany(cascade = CascadeType.ALL)
  List<MUser> users;

  public MRole() {

  }

  public MRole(String roleName) {
    this.roleName = roleName;
  }

  public Integer getRoleid() {
    return roleid;
  }

  public void setRoleid(Integer roleid) {
    this.roleid = roleid;
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
      if (roleid.equals(rhs.roleid)) {
        if (roleid == 0) {
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
    if (roleid != null && roleid != 0) {
      int rid = roleid;
      return (int) (rid ^ (rid >>> 32));
    }
    return super.hashCode();
  }

}
