package org.tests.model.basic;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;

@MappedSuperclass
public class BasicDomain extends Model {

  @Id
  long id;

  @WhenCreated
  Instant whenCreated;

  @WhenModified
  Instant whenModified;

  @Version
  long version;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public Instant getWhenCreated() {
    return whenCreated;
  }

  public void setWhenCreated(Instant whenCreated) {
    this.whenCreated = whenCreated;
  }

  public Instant getWhenModified() {
    return whenModified;
  }

  public void setWhenModified(Instant whenModified) {
    this.whenModified = whenModified;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }
}
