package org.tests.transaction;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.Transaction;
import io.ebean.annotation.IgnorePlatform;
import io.ebean.annotation.Platform;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tests.model.basic.UTDetail;
import org.tests.model.basic.UTMaster;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestBatchPersistCascade extends BaseTestCase {

  Logger logger = LoggerFactory.getLogger(TestBatchPersistCascade.class);

  @Test
  @IgnorePlatform(Platform.HANA)
  public void test() {

    Database ebeanServer = DB.getDefault();

    LoggedSql.start();

    Transaction txn = ebeanServer.beginTransaction();
    try {
      txn.setBatchMode(true);
      logger.info("start ------------");

      for (int i = 0; i < 3; i++) {
        UTMaster master = createMaster(i);
        logger.info("save ------------ {}", i);
        ebeanServer.save(master);
        //txn.flushBatch();
      }

      logger.info("commit ------------");
      txn.commit();

    } finally {
      txn.end();
    }

    List<String> loggedSql = LoggedSql.stop();
    assertTrue(loggedSql.size() > 2);


    testUpdates();

  }

  private void testUpdates() {

    Database server = DB.getDefault();

    List<UTMaster> list = server.find(UTMaster.class).fetch("details").findList();

    Transaction txn = server.beginTransaction();
    try {
      txn.setBatchMode(true);
      txn.setBatchOnCascade(true);

      for (int i = 0; i < 3; i++) {
        UTMaster master = createMaster(i + 500);
        logger.info("save ------------ {}", i);
        server.save(master);
      }

      logger.info("starting updates ------------ ");

      UTMaster lastMaster = null;
      for (UTMaster utMaster : list) {
        utMaster.setName(utMaster.getName() + " + mod");
        List<UTDetail> details = utMaster.getDetails();
        for (UTDetail detail : details) {
          detail.setQty(detail.getQty() + 7);
          detail.setName(detail.getName() + " + foo");
        }

        server.save(utMaster);
        lastMaster = utMaster;
      }

      logger.info("starting some inserts ------------ ");

      for (int i = 0; i < 3; i++) {
        UTMaster master = createMaster(i + 1000);
        logger.info("save ------------ {}", i);
        server.save(master);
        if (i == 1) {
          logger.info("save lastMaster ------------ ");
          lastMaster.setName("mod");
          server.save(lastMaster);
        }
      }

      logger.info("commit ------------ ");

      server.commitTransaction();
    } finally {
      server.endTransaction();
    }

  }

  private UTDetail createUTDetail(String master, int count) {
    UTDetail detail = new UTDetail();
    detail.setName(master + "-" + count);
    detail.setAmount(50d);
    detail.setQty(count);
    return detail;
  }

  private UTMaster createMaster(int count) {

    String name = "master" + count;

    UTMaster m0 = new UTMaster();
    m0.setName(name);
    for (int i = 0; i < 5; i++) {
      m0.addDetail(createUTDetail(name, i));
    }
    return m0;
  }

}
