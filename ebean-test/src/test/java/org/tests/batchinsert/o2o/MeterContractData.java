package org.tests.batchinsert.o2o;

import io.ebean.annotation.NotNull;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import java.util.UUID;

@Entity
public class MeterContractData {
  @Id
  private UUID id;

  @NotNull
  @OneToOne(cascade = CascadeType.ALL)
  private MeterSpecialNeedsClient specialNeedsClient;

  public MeterSpecialNeedsClient getSpecialNeedsClient() {
    return specialNeedsClient;
  }

  public void setSpecialNeedsClient(MeterSpecialNeedsClient specialNeedsClient) {
    this.specialNeedsClient = specialNeedsClient;
  }
}
