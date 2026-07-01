package org.tests.model.m2o;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class MTJOrder {

  @Id
  private long id;

  @Column(name = "org_id")
  private long orgId;

  @Column
  private String other;

  public Long id() {
    return id;
  }

  public MTJOrder setId(Long id) {
    this.id = id;
    return this;
  }

  public Long orgId() {
    return orgId;
  }

  public MTJOrder setOrgId(Long orgId) {
    this.orgId = orgId;
    return this;
  }

  public String other() {
    return other;
  }

  public MTJOrder setOther(String other) {
    this.other = other;
    return this;
  }
}
