package org.tests.model.bridge;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@IdClass(BEmbId.class)
public class BSiteUserD {

  @Id
  private UUID siteId;

  @Id
  private UUID userId;

  private BAccessLevel accessLevel;

  @OneToMany(cascade = CascadeType.PERSIST)
  @JoinColumns({@JoinColumn(name="site_id", referencedColumnName = "site_id"), @JoinColumn(name="user_id", referencedColumnName = "user_id")})
  private List<BSiteUserDMany> children;

  @Version
  private long version;

  public BSiteUserD(BAccessLevel accessLevel, UUID siteId, UUID userId) {
    this.accessLevel = accessLevel;
    this.siteId = siteId;
    this.userId = userId;
  }

  public UUID getSiteId() {
    return siteId;
  }

  public void setSiteId(UUID siteId) {
    this.siteId = siteId;
  }

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public BAccessLevel getAccessLevel() {
    return accessLevel;
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

  public List<BSiteUserDMany> children() {
    return children;
  }

  public void setChildren(List<BSiteUserDMany> children) {
    this.children = children;
  }
}
