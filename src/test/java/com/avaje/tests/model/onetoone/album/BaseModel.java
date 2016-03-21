package com.avaje.tests.model.onetoone.album;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.SoftDelete;
import com.avaje.ebean.annotation.WhenCreated;
import com.avaje.ebean.annotation.WhenModified;
import org.joda.time.DateTime;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class BaseModel extends Model {

  @Id
  protected Long id;

  @SoftDelete
  @Column(name = "deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
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
