package org.tests.model.converstation;

import org.tests.model.BaseModel;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "c_group")
public class Group extends BaseModel {

  boolean inactive;

  String name;

  @OneToMany(mappedBy = "group")
  List<User> users;

  public boolean isInactive() {
    return inactive;
  }

  public void setInactive(boolean inactive) {
    this.inactive = inactive;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<User> getUsers() {
    return users;
  }

  public void setUsers(List<User> users) {
    this.users = users;
  }

}
