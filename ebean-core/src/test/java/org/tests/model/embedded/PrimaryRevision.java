package org.tests.model.embedded;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Version;

@Entity
public class PrimaryRevision {

  @EmbeddedId
  @AttributeOverride(name = "primaryId", column = @Column(name = "id"))
  private RevisionId revisionId;

  String name;

  @Version
  long version;

  public PrimaryRevision(long someId) {
    this.revisionId = new RevisionId(someId, 1);
  }

  public RevisionId getRevisionId() {
    return revisionId;
  }

  public void setRevisionId(RevisionId revisionId) {
    this.revisionId = revisionId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }
}
