package org.tests.model.onetoone.album;

import io.ebean.Model;
import io.ebean.annotation.SoftDelete;
import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;
import org.joda.time.DateTime;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class BaseModel extends Model {

  @Id
  protected Long id;

  @SoftDelete
  protected boolean deleted;

  @WhenCreated
  protected DateTime createdAt;

  @WhenModified
  protected DateTime lastUpdate;

  protected BaseModel() {
  }

  public Long getId() {
    return this.id;
  }

  public DateTime getCreatedAt() {
    return this.createdAt;
  }

  public DateTime getLastUpdate() {
    return this.lastUpdate;
  }

  /**
   * Check if this entry is soft deleted.
   */
  public boolean isDeleted() {
    return this.deleted;
  }

}
