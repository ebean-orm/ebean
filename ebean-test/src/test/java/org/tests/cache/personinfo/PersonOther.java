package org.tests.cache.personinfo;

import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import javax.validation.constraints.Size;
import java.time.Instant;

@Entity
public class PersonOther {

  @Id
  @Size(max=128)
  private String id;

  private String email;

  @WhenCreated
  private Instant whenCreated;

  @WhenModified
  private Instant whenModified;

  @Version
  private long version;

  public PersonOther(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
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
