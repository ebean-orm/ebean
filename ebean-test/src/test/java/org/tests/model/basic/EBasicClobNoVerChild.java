package org.tests.model.basic;

import io.ebean.annotation.SoftDelete;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class EBasicClobNoVerChild {

  @Id
  private Long id;

  @ManyToOne
  private final EBasicClobNoVer parent;

  private final String child;

  @SoftDelete
  private boolean deleted;

  public EBasicClobNoVerChild(EBasicClobNoVer parent, String child) {
    this.parent = parent;
    this.child = child;
  }

  public Long id() {
    return id;
  }

  public EBasicClobNoVerChild id(Long id) {
    this.id = id;
    return this;
  }

  public String child() {
    return child;
  }

  public boolean deleted() {
    return deleted;
  }

  public EBasicClobNoVerChild deleted(boolean deleted) {
    this.deleted = deleted;
    return this;
  }
}
