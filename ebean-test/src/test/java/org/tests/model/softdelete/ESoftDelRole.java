package org.tests.model.softdelete;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import java.util.List;

@Entity
public class ESoftDelRole extends BaseSoftDelete {

  String roleName;

  @ManyToMany(cascade = CascadeType.ALL)
  List<ESoftDelUser> users;

  public ESoftDelRole(String roleName) {
    this.roleName = roleName;
  }

  public String getRoleName() {
    return roleName;
  }

  public void setRoleName(String roleName) {
    this.roleName = roleName;
  }

  public List<ESoftDelUser> getUsers() {
    return users;
  }

  public void setUsers(List<ESoftDelUser> users) {
    this.users = users;
  }
}
