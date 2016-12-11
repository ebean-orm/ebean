package org.tests.model.basic;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OrderBy;
import javax.persistence.Version;
import java.util.ArrayList;
import java.util.List;

@Entity
public class MnocUser {

  @Id
  Integer userId;

  String userName;

  @Version
  Integer version;

  // No cascade REMOVE
  @ManyToMany(cascade = CascadeType.PERSIST)
  @OrderBy("roleName")
  List<MnocRole> validRoles;

  public MnocUser() {

  }

  public MnocUser(String userName) {
    this.userName = userName;
  }

  public Integer getUserId() {
    return userId;
  }

  public void setUserId(Integer roleId) {
    this.userId = roleId;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String roleName) {
    this.userName = roleName;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public List<MnocRole> getValidRoles() {
    return validRoles;
  }

  public void setValidRoles(List<MnocRole> validRoles) {
    this.validRoles = validRoles;
  }

  public void addValidRole(MnocRole role) {
    if (validRoles == null) {
      validRoles = new ArrayList<>();
    }
    validRoles.add(role);
  }
}
