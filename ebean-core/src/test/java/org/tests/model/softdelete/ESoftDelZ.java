package org.tests.model.softdelete;

import io.ebean.annotation.SoftDelete;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity
public class ESoftDelZ {

  @Id
  private Long id;

  @SoftDelete
  private boolean deleted;

  private UUID uuid;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  public UUID getUuid() {
    return uuid;
  }

  public void setUuid(UUID uuid) {
    this.uuid = uuid;
  }
}
