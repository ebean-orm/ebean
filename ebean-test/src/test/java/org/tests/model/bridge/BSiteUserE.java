package org.tests.model.bridge;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.ManyToOne;

@Entity
@IdClass(BEmbId.class)
public class BSiteUserE {

  @Id
  @ManyToOne
  private final BSite site;

  @Id
  @ManyToOne
  private final BUser user;

  private BAccessLevel accessLevel;


  public BSiteUserE(BAccessLevel accessLevel, BSite site, BUser user) {
    this.accessLevel = accessLevel;
    this.site = site;
    this.user = user;
  }

  public BAccessLevel getAccessLevel() {
    return accessLevel;
  }

  public BSite getSite() {
    return site;
  }

  public BUser getUser() {
    return user;
  }

  public void setAccessLevel(BAccessLevel accessLevel) {
    this.accessLevel = accessLevel;
  }
}
