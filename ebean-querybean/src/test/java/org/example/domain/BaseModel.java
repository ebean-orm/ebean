package org.example.domain;

import io.ebean.Model;
import io.ebean.annotation.CreatedTimestamp;
import io.ebean.annotation.UpdatedTimestamp;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import java.sql.Timestamp;

/**
 * Base domain object with Id, version, whenCreated and whenUpdated.
 * 
 * <p>
 * Extending Model to enable the 'active record' style.
 * 
 * <p>
 * whenCreated and whenUpdated are generally useful for maintaining external search services (like
 * elasticsearch) and audit.
 */
@MappedSuperclass
public abstract class BaseModel extends Model {

  @Id
  Long id;

  @Version
  Long version;

  @CreatedTimestamp
  Timestamp whenCreated;

  @UpdatedTimestamp
  Timestamp whenUpdated;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  public Timestamp getWhenCreated() {
    return whenCreated;
  }

  public void setWhenCreated(Timestamp whenCreated) {
    this.whenCreated = whenCreated;
  }

  public Timestamp getWhenUpdated() {
    return whenUpdated;
  }

  public void setWhenUpdated(Timestamp whenUpdated) {
    this.whenUpdated = whenUpdated;
  }

}
