package org.tests.model.compositekeys;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Version;
import java.util.UUID;

@Entity
@IdClass(CKEmbId.class)
public class CKSiteUser {

  @Id
  private UUID siteId;

  @Id
  private UUID userId;

  private String accessLevel;

  @Version
  private long version;

  public CKSiteUser(String accessLevel, UUID siteId, UUID userId) {
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


  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }

}
