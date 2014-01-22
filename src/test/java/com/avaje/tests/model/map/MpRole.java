package com.avaje.tests.model.map;

import javax.persistence.*;

@Entity
public class MpRole {

  @Id
  private Long id;

  private Long organizationId;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getOrganizationId() {
    return organizationId;
  }

  public void setOrganizationId(Long organizationId) {
    this.organizationId = organizationId;
  }
}