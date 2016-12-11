package org.tests.model.softdelete;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import java.util.List;

@Entity
public class ESoftDelUser extends BaseSoftDelete {

  String userName;

  @ManyToMany(cascade = CascadeType.ALL)
  List<ESoftDelRole> roles;

  public ESoftDelUser(String userName) {
    this.userName = userName;
  }

  public ESoftDelUser() {
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public List<ESoftDelRole> getRoles() {
    return roles;
  }

  public void setRoles(List<ESoftDelRole> roles) {
    this.roles = roles;
  }
}
