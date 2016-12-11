package org.tests.compositekeys.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Entity
public class ParcelLocation {
  @Id
  @Column(name = "parcelLocId")
  private Long parcelLocId;

  private String location;

  @OneToOne
  @JoinColumn(name = "parcelId", referencedColumnName = "parcelId")
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
