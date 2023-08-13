package org.tests.cache.embeddedid;

import javax.persistence.Embeddable;

@Embeddable
public class ConceptId {
  private final String id;
  private final String networkId;

  public ConceptId(String networkId, String id) {
    this.networkId = networkId;
    this.id = id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ConceptId conceptId = (ConceptId) o;
    if (!id.equals(conceptId.id)) return false;
    return networkId.equals(conceptId.networkId);
  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + networkId.hashCode();
    return result;
  }
}
