package org.tests.model.bridge;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.UUID;

@Entity
public class BSiteUser {

  @Embeddable
  public static class Id {

    // Use siteId, userId matching by db column naming convention
    public UUID siteId;
    public UUID userId;

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Id id = (Id) o;
      if (!siteId.equals(id.siteId)) return false;
      return userId.equals(id.userId);
    }

    @Override
    public int hashCode() {
      int result = siteId.hashCode();
      result = 31 * result + userId.hashCode();
      return result;
    }
  }

  @EmbeddedId
  Id id;

  private BAccessLevel accessLevel;

  @ManyToOne(optional = false)
  private final BSite site;

  @ManyToOne(optional = false)
  private final BUser user;


  public BSiteUser(BAccessLevel accessLevel, BSite site, BUser user) {
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
