package org.tests.model.converstation;

import io.ebean.annotation.History;
import io.ebean.annotation.HistoryExclude;
import org.tests.model.BaseModel;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@History
@Entity
@Table(name = "c_user")
public class User extends BaseModel {

  boolean inactive;

  String name;

  String email;

  @HistoryExclude
  String passwordHash;

  @ManyToOne
  Group group;

  public User() {
  }

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

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

}
