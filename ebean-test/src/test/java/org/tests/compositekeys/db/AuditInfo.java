package org.tests.compositekeys.db;

import javax.persistence.Embeddable;
import java.util.Date;

@Embeddable
public class AuditInfo {
  private Date lastUpdated;
  private Date created;
  private String updatedBy;
  private String createdBy;

  public AuditInfo() {
    created = new Date();
    createdBy = "dummy";
  }

  public Date getLastUpdated() {
    return lastUpdated;
  }

  public void setLastUpdated(Date lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  public Date getCreated() {
    return created;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }
}
