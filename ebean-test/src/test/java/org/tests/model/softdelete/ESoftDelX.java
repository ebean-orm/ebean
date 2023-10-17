package org.tests.model.softdelete;

import io.ebean.annotation.SoftDelete;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import java.util.UUID;

@Entity
public class ESoftDelX {

  @Id
  private UUID id;

  @OneToOne
  private ESoftDelY y;

  @ManyToOne
  private ESoftDelZ organization;

  @SoftDelete
  boolean deleted;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public ESoftDelY getY() {
    return y;
  }

  public void setY(ESoftDelY y) {
    this.y = y;
  }

  public ESoftDelZ getOrganization() {
    return organization;
  }

  public void setOrganization(ESoftDelZ organization) {
    this.organization = organization;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }
}
