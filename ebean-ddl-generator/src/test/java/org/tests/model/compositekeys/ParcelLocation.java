package org.tests.model.compositekeys;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

@Entity
public class ParcelLocation {
  @Id
  @Column(name = "parcellocid")
  private Long parcelLocId;

  private String location;

  @OneToOne
  @JoinColumn(name = "parcelid", referencedColumnName = "parcelid")
  private Parcel parcel;

  public Long getParcelLocId() {
    return parcelLocId;
  }

  public void setParcelLocId(Long parcelLocId) {
    this.parcelLocId = parcelLocId;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }


  public Parcel getParcel() {
    return parcel;
  }

  public void setParcel(Parcel parcel) {
    this.parcel = parcel;
  }
}
