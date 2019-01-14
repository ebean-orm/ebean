package org.tests.batchinsert.o2o;

import io.ebean.annotation.NotNull;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Id;

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
