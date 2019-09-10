package org.tests.batchinsert;

import io.ebean.Ebean;
import io.ebean.annotation.PersistBatch;
import io.ebean.annotation.Transactional;
import org.junit.Test;
import org.tests.batchinsert.o2o.MeterAddressData;
import org.tests.batchinsert.o2o.MeterContractData;
import org.tests.batchinsert.o2o.MeterSpecialNeedsClient;
import org.tests.batchinsert.o2o.MeterSpecialNeedsContact;
import org.tests.batchinsert.o2o.MeterVersion;

public class TestBatchInsertOneToOne {
  private MeterVersion build(){
    MeterAddressData addressData = new MeterAddressData();
    addressData.setStreet("street");
    MeterContractData contractData = new MeterContractData();

    MeterSpecialNeedsClient specialNeedsClient = new MeterSpecialNeedsClient();
    contractData.setSpecialNeedsClient(specialNeedsClient);

    MeterSpecialNeedsContact contact1 = new MeterSpecialNeedsContact();

    specialNeedsClient.setPrimary(contact1);

    MeterVersion meterVersion = new MeterVersion();
    meterVersion.setAddressData(addressData);
    meterVersion.setContractData(contractData);

    return meterVersion;
  }

  @Transactional(batch = PersistBatch.ALL, batchOnCascade = PersistBatch.ALL, batchSize = 10)
  @Test
  public void test() {
    MeterVersion meterVersion = build();

    Ebean.save(meterVersion);
  }
}
