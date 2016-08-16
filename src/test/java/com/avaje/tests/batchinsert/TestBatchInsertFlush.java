package com.avaje.tests.batchinsert;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.annotation.Transactional;
import com.avaje.ebean.config.PersistBatch;
import com.avaje.tests.model.basic.EBasicVer;
import org.junit.Test;

import java.sql.Timestamp;

import static org.junit.Assert.assertNotNull;

public class TestBatchInsertFlush extends BaseTestCase {

  @Test
  @Transactional(batch = PersistBatch.ALL)
  public void transactional_flushOnGetId() {

    EbeanServer server = Ebean.getDefaultServer();

    EBasicVer b1 = new EBasicVer("b1");
    server.save(b1);

    EBasicVer b2 = new EBasicVer("b2");
    server.save(b2);

    Integer id = b1.getId();
    assertNotNull(id);
    EBasicVer b3 = new EBasicVer("b3");
    server.save(b3);
  }

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

      Integer id = b1.getId();
      assertNotNull(id);

      EBasicVer b3 = new EBasicVer("b3");
      server.save(b3, txn);

      txn.commit();

    } finally {
      txn.end();
    }
  }

  @Test
  public void testFlushOnGetProperty() {

    EbeanServer server = Ebean.getDefaultServer();
    Transaction txn = server.beginTransaction();
    try {
      txn.setBatch(PersistBatch.ALL);

      EBasicVer b1 = new EBasicVer("b1");
      server.save(b1, txn);

      EBasicVer b2 = new EBasicVer("b2");
      server.save(b2, txn);

      // flush here
      Timestamp lastUpdate = b1.getLastUpdate();
      assertNotNull(lastUpdate);

      EBasicVer b3 = new EBasicVer("b3");
      server.save(b3, txn);

      txn.commit();

    } finally {
      txn.end();
    }
  }

  @Test
  public void testFlushOnSetProperty() {

    EbeanServer server = Ebean.getDefaultServer();
    Transaction txn = server.beginTransaction();
    try {
      txn.setBatch(PersistBatch.ALL);

      EBasicVer b1 = new EBasicVer("b1");
      server.save(b1, txn);

      EBasicVer b2 = new EBasicVer("b2");
      server.save(b2, txn);

      // flush here
      b1.setDescription("modify");

      EBasicVer b3 = new EBasicVer("b3");
      server.save(b3, txn);

      txn.commit();

    } finally {
      txn.end();
    }
  }
}
