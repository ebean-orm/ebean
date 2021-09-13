package org.tests.model.softdelete;

import io.ebean.annotation.SoftDelete;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity
public class ESoftDelY {

  @Id
  private Long id;

  @ManyToOne
  private ESoftDelZ organization;

  @OneToOne(mappedBy = "y")
  private ESoftDelX x;

  @SoftDelete
  boolean deleted;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public ESoftDelZ getOrganization() {
    return organization;
  }

  public void setOrganization(ESoftDelZ organization) {
    this.organization = organization;
  }

  public ESoftDelX getX() {
    return x;
  }

  public void setX(ESoftDelX x) {
    this.x = x;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }
}
