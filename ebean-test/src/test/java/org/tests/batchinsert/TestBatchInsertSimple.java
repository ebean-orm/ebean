package org.tests.batchinsert;

import io.ebean.DB;
import io.ebean.Transaction;
import io.ebean.annotation.PersistBatch;
import io.ebean.annotation.Platform;
import io.ebean.annotation.Transactional;
import io.ebean.xtest.BaseTestCase;
import io.ebean.xtest.IgnorePlatform;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.UTDetail;
import org.tests.model.basic.UTMaster;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.assertNull;

public class TestBatchInsertSimple extends BaseTestCase {

  Random random = new Random();

  @Test
  public void testJdbcBatchPerRequestWithMasterAndDetails() {

    int numOfMasters = 4;// 2 + random.nextInt(8);

    Transaction transaction = DB.beginTransaction();
    try {
      transaction.setBatchMode(false);
      transaction.setBatchOnCascade(true);
      transaction.setBatchSize(30);
      // setBatchGetGeneratedKeys MUST be turned off for MS SQL Server because :(
      transaction.setGetGeneratedKeys(false);

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
  @IgnorePlatform(Platform.HANA)
  public void testTransactional() {

    saveWithFullBatchMode();
  }

  @Transactional(batchSize = 50)
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
  public void testBatchCollision() {

    UTMaster m1 = new UTMaster();
    m1.setId(1000);
    m1.save();


    try (Transaction transaction = DB.beginTransaction()) {
      transaction.setBatchMode(true);

      UTMaster m2 = new UTMaster();
      m2.setId(1000);
      m2.save();
      UTMaster m3 = new UTMaster();
      m3.setId(1001);
      m3.save();
      // we expect the IDs of the affected models to make debugging easier.
      assertThatThrownBy(transaction::commit)
        .hasMessageContaining("Error when batch flush on: [1000, 1001], sql: insert into ut_master");

    }
  }

//  @Test
//  public void testTransactional_skipGeneratedBeans() {
//
//    if (isMsSqlServer()) return;
//
//    List<UTMaster> beans = saveWithFullBatchMode_skipGeneratedKeys();
//    for (UTMaster bean : beans) {
//      assertNull(bean.getId());
//    }
//  }
//
//  @Transactional(batch=PersistBatch.ALL, batchSize=50, getGeneratedKeys = false)
//  public List<UTMaster> saveWithFullBatchMode_skipGeneratedKeys() {
//
//    Transaction transaction = server().currentTransaction();
//    SpiTransaction spiTxn = (SpiTransaction)transaction;
//    Boolean generatedKeys = spiTxn.getBatchGetGeneratedKeys();
//
//    assertThat(generatedKeys).isFalse();
//
//    List<UTMaster> beans = new ArrayList<UTMaster>();
//    for (int i = 0; i < 4; i++) {
//     beans.add(createMaster(i));
//    }
//
//    server().saveAll(beans);
//    return beans;
//  }

  @Test
  public void testJdbcBatchPerRequestWithMasterOnly() {

    int numOfMasters = 4;

    Transaction transaction = DB.beginTransaction();
    try {
      transaction.setBatchMode(false);
      transaction.setBatchOnCascade(true);
      transaction.setBatchSize(30);
      // setBatchGetGeneratedKeys MUST be turned off for MS SQL Server because :(
      transaction.setGetGeneratedKeys(false);

      for (int i = 0; i < numOfMasters; i++) {
        UTMaster master = createMaster(i);
        DB.save(master);
      }

      transaction.commit();

    } finally {
      transaction.end();
    }
  }

  @Test
  public void testJdbcBatchOnCollection() {

    int numOfMasters = 3;

    List<UTMaster> masters = new ArrayList<>();
    for (int i = 0; i < numOfMasters; i++) {
      masters.add(createMasterAndDetails(i, 7));
    }

    Transaction transaction = DB.beginTransaction();
    try {
      transaction.setBatchMode(true);
      transaction.setBatchOnCascade(PersistBatch.ALL.equals(spiEbeanServer().databasePlatform().persistBatchOnCascade()));
      transaction.setBatchSize(20);

      // escalate based on batchOnCascade value
      DB.saveAll(masters);

      transaction.commit();

    } finally {
      transaction.end();
    }
  }

  @Test
  public void testJdbcBatchOnCollectionNoTransaction() {

    int numOfMasters = 3;

    List<UTMaster> masters = new ArrayList<>();
    for (int i = 0; i < numOfMasters; i++) {
      masters.add(createMasterAndDetails(i, 5));
    }

    // escalate based on batchOnCascade value
    DB.saveAll(masters);

    for (int i = 0; i < masters.size(); i++) {
      UTMaster utMaster = masters.get(i);
      utMaster.setName(utMaster.getName() + "-Mod");
      if (i % 2 == 0) {
        // make the updates a little bit different
        utMaster.setDescription("Blah");
      }
    }

    DB.saveAll(masters);
  }

  private UTMaster createMasterAndDetails(int masterPos, int size) {

    UTMaster master = createMaster(masterPos);
    List<UTDetail> details = new ArrayList<>();

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

  @Test
  public void npe_addBatch_withoutAnyBindParams() {
    try (Transaction txn = DB.beginTransaction()) {
      // don't write code like this please ...
      DB.sqlUpdate("update ut_master set name='DoNotDoThisPlease' where id=999999999").addBatch();
      txn.commit();
    }

    // don't write code like the above but use bind values like:
    try (Transaction txn = DB.beginTransaction()) {
      DB.sqlUpdate("update ut_master set name=? where id=?")
        .setParameters("DoNotDoThisPlease", 999999999)
        .addBatch();

      txn.commit();
    }
  }
}
