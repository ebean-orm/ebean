package org.tests.batchinsert.o2o;

import io.ebean.annotation.NotNull;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.util.UUID;

@Entity
public class MeterVersion {
  @Id
  private UUID id;

  //@NotNull
  @OneToOne(cascade = CascadeType.ALL)
  private MeterAddressData addressData;

  @NotNull
  @OneToOne(cascade = CascadeType.ALL)
  private MeterContractData contractData;

  public MeterAddressData getAddressData() {
    return addressData;
  }

  public void setAddressData(MeterAddressData addressData) {
    this.addressData = addressData;
  }

  public MeterContractData getContractData() {
    return contractData;
  }

  public void setContractData(MeterContractData contractData) {
    this.contractData = contractData;
  }
}
