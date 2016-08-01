package com.avaje.tests.batchinsert;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.config.PersistBatch;
import com.avaje.tests.model.basic.EBasicVer;
import org.junit.Test;

import java.sql.Timestamp;

public class TestBatchInsertFlush extends BaseTestCase {

  @Test
  public void testFlushOnGetId() {

    EbeanServer server = Ebean.getDefaultServer();
    Transaction txn = server.beginTransaction();
    try {
      txn.setBatch(PersistBatch.ALL);

      EBasicVer b1 = new EBasicVer("b1");
      server.save(b1, txn);

      EBasicVer b2 = new EBasicVer("b2");
      server.save(b2, txn);

      //txn.flushBatch();

      b1.setDescription("modify");
      System.out.println("here");
      Timestamp lastUpdate = b1.getLastUpdate();
      Integer id = b1.getId();

      EBasicVer b3 = new EBasicVer("b3");
      server.save(b3, txn);

      txn.commit();

    } finally {
      txn.end();
    }


  }
}
