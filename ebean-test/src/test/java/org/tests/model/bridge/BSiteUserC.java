package org.tests.model.bridge;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.UUID;

@Entity
public class BSiteUserC {

  @Embeddable
  public static class Id {

    // matching by @JoinColumn (not naming convention or property name)
    public UUID siteUid;
    public UUID userUid;

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Id id = (Id) o;
      if (!siteUid.equals(id.siteUid)) return false;
      return userUid.equals(id.userUid);
    }

    @Override
    public int hashCode() {
      int result = siteUid.hashCode();
      result = 31 * result + userUid.hashCode();
      return result;
    }
  }

  @EmbeddedId
  Id id;

  private BAccessLevel accessLevel;

  @ManyToOne(optional = false)
  @JoinColumn(name = "site_uid", insertable = false, updatable = false)
  private final BSite site;

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_uid", insertable = false, updatable = false)
  private final BUser user;


  public BSiteUserC(BAccessLevel accessLevel, BSite site, BUser user) {
    this.accessLevel = accessLevel;
    this.site = site;
    this.user = user;
  }

  public Id getId() {
    return id;
  }

  public void setId(Id id) {
    this.id = id;
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
