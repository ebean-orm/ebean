package org.tests.model.map;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class MpRole {

  @Id
  private Long id;

  private String code;

  private Long organizationId;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public Long getOrganizationId() {
    return organizationId;
  }

  public void setOrganizationId(Long organizationId) {
    this.organizationId = organizationId;
  }
}
