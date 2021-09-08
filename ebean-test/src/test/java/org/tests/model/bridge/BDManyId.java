package org.tests.model.bridge;

import javax.persistence.Embeddable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class BDManyId {

  private final UUID siteId;
  private final UUID userId;
  private final String name;

  public BDManyId(UUID siteId, UUID userId, String name) {
    this.siteId = siteId;
    this.userId = userId;
    this.name = name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BDManyId bdManyId = (BDManyId) o;
    return siteId.equals(bdManyId.siteId) && userId.equals(bdManyId.userId) && name.equals(bdManyId.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(siteId, userId, name);
  }
}
