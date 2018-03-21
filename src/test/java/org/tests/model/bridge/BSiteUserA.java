package org.tests.model.bridge;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Version;
import java.util.Objects;
import java.util.UUID;

@Entity
public class BSiteUserA {

  @Embeddable
  public static class Id {

    // Use siteId, userId matching by db column naming convention
    public UUID siteId;
    public UUID userId;

    public Id(UUID siteId, UUID userId) {
      this.siteId = siteId;
      this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Id that = (Id) o;
      return Objects.equals(siteId, that.siteId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(siteId, userId);
    }
  }

  @EmbeddedId
  Id id;

  private BAccessLevel accessLevel;

  @ManyToOne(optional = false)
  private final BSite site;

  @ManyToOne(optional = false)
  private final BUser user;

  @Version
  private long version;

  public BSiteUserA(BAccessLevel accessLevel, BSite site, BUser user) {
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

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }
}
