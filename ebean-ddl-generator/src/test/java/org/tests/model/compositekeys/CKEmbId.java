package org.tests.model.compositekeys;

import javax.persistence.Embeddable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class CKEmbId {

  private UUID siteId;
  private UUID userId;

  public CKEmbId(UUID siteId, UUID userId) {
    this.siteId = siteId;
    this.userId = userId;
  }

  public UUID getSiteId() {
    return siteId;
  }

  public UUID getUserId() {
    return userId;
  }

  @Override
  public String toString() {
    return "st:" + siteId + " ui:" + userId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CKEmbId that = (CKEmbId) o;
    return Objects.equals(siteId, that.siteId) && Objects.equals(userId, that.userId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(siteId, userId);
  }

  /**
   * Just simulating the hash from Objects.hash(...)
   */
  int otherHash() {
    int result = 31 + siteId.hashCode();
    result = 31 * result + userId.hashCode();
    return result;
  }
}
