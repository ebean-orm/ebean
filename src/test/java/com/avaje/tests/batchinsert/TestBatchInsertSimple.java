package com.avaje.tests.batchinsert;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.annotation.Transactional;
import com.avaje.ebean.config.PersistBatch;
import com.avaje.tests.model.basic.UTDetail;
import com.avaje.tests.model.basic.UTMaster;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestBatchInsertSimple extends BaseTestCase {

  Random random = new Random();

  @Test
  public void testJdbcBatchPerRequestWithMasterAndDetails() {

    int numOfMasters = 4;// 2 + random.nextInt(8);

    Transaction transaction = Ebean.beginTransaction();
    try {
      transaction.setBatch(PersistBatch.NONE);
      transaction.setBatchOnCascade(PersistBatch.INSERT);
      transaction.setBatchSize(30);

      for (int i = 0; i < numOfMasters; i++) {
        UTMaster master = createMasterAndDetails(i, 20);
        master.save();
      }

      transaction.commit();

    } finally {
      transaction.end();
    }
  }

  @Test
  public void testTransactional() {

    saveWithFullBatchMode();
  }

  @Transactional(batch=PersistBatch.ALL, batchSize=50)
  public void saveWithFullBatchMode() {

    int numOfMasters = 4;

    for (int i = 0; i < numOfMasters; i++) {
      UTMaster master = createMasterAndDetails(i, 5);
      // the save is 'batched' and does not execute immediately
      // ... it now acts more like 'merge/persist'
      master.save();
    }
  }

  @Test
  public void testJdbcBatchPerRequestWithMasterOnly() {

    int numOfMasters = 4;

    Transaction transaction = Ebean.beginTransaction();
    try {
      transaction.setBatch(PersistBatch.NONE);
      transaction.setBatchOnCascade(PersistBatch.INSERT);
      transaction.setBatchSize(30);

      for (int i = 0; i < numOfMasters; i++) {
        UTMaster master = createMaster(i);
        Ebean.save(master);
      }

      transaction.commit();

    } finally {
      transaction.end();
    }
  }

  @Test
  public void testJdbcBatchOnCollection() {

    int numOfMasters = 3;

    List<UTMaster> masters = new ArrayList<UTMaster>();
    for (int i = 0; i < numOfMasters; i++) {
      masters.add(createMasterAndDetails(i, 7));
    }

    Transaction transaction = Ebean.beginTransaction();
    try {
      transaction.setBatch(PersistBatch.NONE);
      transaction.setBatchOnCascade(PersistBatch.ALL);
      transaction.setBatchSize(20);

      // escalate based on batchOnCascade value
      Ebean.saveAll(masters);

      transaction.commit();

    } finally {
      transaction.end();
    }
  }

  @Test
  public void testJdbcBatchOnCollectionNoTransaction() {

    int numOfMasters = 3;

    List<UTMaster> masters = new ArrayList<UTMaster>();
    for (int i = 0; i < numOfMasters; i++) {
      masters.add(createMasterAndDetails(i, 5));
    }

    // escalate based on batchOnCascade value
    Ebean.saveAll(masters);

    for (int i = 0; i < masters.size() ; i++) {
      UTMaster utMaster = masters.get(i);
      utMaster.setName(utMaster.getName()+"-Mod");
      if (i % 2 == 0) {
        // make the updates a little bit different
        utMaster.setDescription("Blah");
      }
    }

    Ebean.saveAll(masters);
  }

  private UTMaster createMasterAndDetails(int masterPos, int size) {

    UTMaster master = createMaster(masterPos);
    List<UTDetail> details = new ArrayList<UTDetail>();

    int count = 2 + random.nextInt(size);

    for (int i = 0; i < count; i++) {

      int qty = 1 + random.nextInt(99);
      double amount = random.nextDouble();

      details.add(createDetail(masterPos + "-" + i, qty, amount));
    }
    master.setDetails(details);
    return master;
  }

  private UTMaster createMaster(int position) {
    UTMaster m = new UTMaster();
    m.setName("batchInsert-master" + position);
    return m;
  }

  private UTDetail createDetail(String position, int qty, double amount) {

    UTDetail detail = new UTDetail();
    detail.setName("batchInsert-detail-" + position);
    detail.setQty(qty);
    detail.setAmount(amount);

    // System.out.println("-- "+detail);

    return detail;
  }

}
