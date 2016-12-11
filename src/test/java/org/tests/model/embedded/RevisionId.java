package org.tests.model.embedded;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class RevisionId {

  Long primaryId;

  Integer revision;

  public RevisionId() {
  }

  public RevisionId(Long primaryId, Integer revision) {
    this.revision = revision;
    this.primaryId = primaryId;
  }

  @Column(name = "revision")
  public Integer getRevision() {
    return revision;
  }

  public void setRevision(Integer revision) {
    this.revision = revision;
  }

  public Long getPrimaryId() {
    return primaryId;
  }

  public void setPrimaryId(Long primaryId) {
    this.primaryId = primaryId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RevisionId that = (RevisionId) o;
    if (!primaryId.equals(that.primaryId)) return false;
    return revision.equals(that.revision);
  }

  @Override
  public int hashCode() {
    int result = primaryId.hashCode();
    result = 31 * result + revision.hashCode();
    return result;
  }
}
