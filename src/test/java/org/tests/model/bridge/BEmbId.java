package org.tests.model.bridge;

import javax.persistence.Embeddable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class BEmbId {

  private UUID siteId;
  private UUID userId;

  public BEmbId(UUID siteId, UUID userId) {
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

    BEmbId that = (BEmbId) o;
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
