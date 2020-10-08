package org.tests.batchinsert.o2o;

import io.ebean.annotation.NotNull;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

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
