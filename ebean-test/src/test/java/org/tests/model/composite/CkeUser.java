package org.tests.model.composite;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;

@Entity
public class CkeUser {

  @EmbeddedId
  private CkeUserKey userPK;

  private String name;

  public CkeUserKey getUserPK() {
    return userPK;
  }

  public void setUserPK(CkeUserKey userPK) {
    this.userPK = userPK;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
