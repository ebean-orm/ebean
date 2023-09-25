package org.tests.model.compositekeys;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Parcel {
  @Id
  @Column(name = "parcelid")
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
