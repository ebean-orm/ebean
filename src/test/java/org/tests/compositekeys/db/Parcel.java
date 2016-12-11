package org.tests.compositekeys.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Parcel {
  @Id
  @Column(name = "parcelId")
  private Long parcelId;

  private String description;

  public Long getParcelId() {
    return parcelId;
  }

  public void setParcelId(Long parcelId) {
    this.parcelId = parcelId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
