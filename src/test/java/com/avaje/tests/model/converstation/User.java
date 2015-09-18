package com.avaje.tests.model.converstation;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.avaje.ebean.annotation.History;
import com.avaje.tests.model.BaseModel;

@History
@Entity
@Table(name="c_user")
public class User extends BaseModel {

  boolean inactive;
  
  String name;
  
  String email;

  @ManyToOne
  Group group;
  
  
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

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Group getGroup() {
    return group;
  }

  public void setGroup(Group group) {
    this.group = group;
  }
  
}
