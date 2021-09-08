package org.tests.model.bridge;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.UUID;

@Entity
public class BSiteUserB {

  @Embeddable
  public static class Id {

    // Use plain site and user matching by property name (not siteId, userId)
    public UUID site;

    @Column(name = "usr") // not using 'user' as column name as PG keyword
    public UUID user;

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Id id = (Id) o;
      if (!site.equals(id.site)) return false;
      return user.equals(id.user);
    }

    @Override
    public int hashCode() {
      int result = site.hashCode();
      result = 31 * result + user.hashCode();
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


  public BSiteUserB(BAccessLevel accessLevel, BSite site, BUser user) {
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
