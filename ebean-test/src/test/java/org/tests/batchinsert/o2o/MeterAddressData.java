package org.tests.batchinsert.o2o;

import io.ebean.annotation.NotNull;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.util.UUID;

@Entity
public class MeterAddressData {
  @Id
  private UUID id;

  @NotNull
  private String street;

  public String getStreet() {
    return street;
  }

  public void setStreet(String street) {
    this.street = street;
  }
}
