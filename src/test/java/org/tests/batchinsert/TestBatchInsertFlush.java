package org.tests.batchinsert;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Transaction;
import io.ebean.annotation.PersistBatch;
import io.ebean.annotation.Transactional;
import io.ebean.meta.BasicMetricVisitor;
import io.ebean.meta.MetaTimedMetric;
import io.ebeaninternal.api.SpiTransaction;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.EBasicVer;
import org.tests.model.basic.TSDetail;
import org.tests.model.basic.TSMaster;

import java.sql.Timestamp;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

public class TestBatchInsertFlush extends BaseTestCase {

  @Test
  public void no_cascade() {

    EbeanServer server = Ebean.getDefaultServer();

    resetAllMetrics();

    Transaction transaction = server.beginTransaction();
    try {
      transaction.setPersistCascade(false);
      transaction.setBatchSize(10);
      transaction.setBatch(PersistBatch.ALL);
      transaction.setLabel("TestBatchInsertFlush.no_cascade");

      LoggedSqlCollector.start();

      TSMaster m = new TSMaster();
      m.setName("master1");
      server.save(m);

      // we don't want this to flush batch yet but instead
      // determine its batch depth based on imported assoc to master
      TSDetail d1 = new TSDetail("d1");
      d1.setMaster(m);
      server.save(d1);

      TSDetail d2 = new TSDetail("d2");
      d2.setMaster(m);
      server.save(d2);

      // we want this to batch flush with master 1
      TSMaster m2 = new TSMaster();
      m2.setName("master2");
      server.save(m2);

      transaction.commit();

      List<String> sql = LoggedSqlCollector.stop();

      // we get the 2 master inserts first
      assertThat(sql.get(0)).contains("insert into t_atable_thatisrelatively");
      assertThat(sql.get(1)).contains("insert into t_atable_thatisrelatively");
      // detail
      assertThat(sql.get(2)).contains("insert into t_detail_with_other_namexxxyy");

      assertThat(((SpiTransaction)transaction).getLabel()).isEqualTo("TestBatchInsertFlush.no_cascade");

    } finally {
      transaction.end();
    }

    BasicMetricVisitor basic = visitMetricsBasic();
    List<MetaTimedMetric> txnStats = basic.getTimedMetrics();
    for (MetaTimedMetric txnMetric : txnStats) {
      System.out.println(txnMetric);
    }
    assertThat(txnStats).hasSize(2);
    assertThat(txnStats.get(0).getName()).isEqualTo("txn.main");
    assertThat(txnStats.get(1).getName()).isEqualTo("txn.named.TestBatchInsertFlush.no_cascade");
  }

  @Test
  @Transactional(batch = PersistBatch.ALL, flushOnQuery = false)
  public void transactional_flushOnQueryFalse() {

    LoggedSqlCollector.start();

    Ebean.save(new EBasicVer("b1"));
    Ebean.save(new EBasicVer("b2"));

    // does not trigger JDBC batch with flushOnQuery = false
    Ebean.find(Customer.class).findCount();

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql.get(0)).contains("select count(*)");
  }

  @Test
  @Transactional(batch = PersistBatch.ALL)
  public void transactional_flushOnQuery() {

    LoggedSqlCollector.start();

    Ebean.save(new EBasicVer("b1"));
    Ebean.save(new EBasicVer("b2"));

    // by default triggers flush of JDBC batch
    Ebean.find(Customer.class).findCount();

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql.get(0)).contains("insert into e_basicver");
  }

  @Test
  @Transactional(batch = PersistBatch.ALL)
  public void transactional_flushOnGetId() {

    EbeanServer server = Ebean.getDefaultServer();
    LoggedSqlCollector.start();
    
    EBasicVer b1 = new EBasicVer("b1");
    server.save(b1);

    EBasicVer b2 = new EBasicVer("b2");
    server.save(b2);

    //flush here
    assertThat(LoggedSqlCollector.current()).isEmpty();
    Integer id = b1.getId();
    assertNotNull(id);
    assertThat(LoggedSqlCollector.current()).hasSize(2);
    
    EBasicVer b3 = new EBasicVer("b3");
    server.save(b3);
  }

  @Test
  public void testFlushOnGetId() {

    EbeanServer server = Ebean.getDefaultServer();
    Transaction txn = server.beginTransaction();
    try {
      LoggedSqlCollector.start();
      txn.setBatchMode(true);

      EBasicVer b1 = new EBasicVer("b1");
      server.save(b1, txn);

      EBasicVer b2 = new EBasicVer("b2");
      server.save(b2, txn);

      //flush here
      assertThat(LoggedSqlCollector.current()).isEmpty();
      Integer id = b1.getId();
      assertNotNull(id);
      assertThat(LoggedSqlCollector.current()).hasSize(2);

      EBasicVer b3 = new EBasicVer("b3");
      server.save(b3, txn);

      txn.commit();

    } finally {
      txn.end();
    }
  }
  
  @Test
  @Transactional(batch = PersistBatch.ALL)
  public void transactional_noflushWhenIdIsLoaded() {

    EbeanServer server = Ebean.getDefaultServer();
      
    LoggedSqlCollector.start();

    EBasicVer b1 = new EBasicVer("b1");
    b1.setId(78965);
    server.save(b1);

    EBasicVer b2 = new EBasicVer("b2");
    b2.setId(78645);
    server.save(b2);

    assertThat(LoggedSqlCollector.current()).isEmpty();
    // dont flush here
    Integer id = b1.getId();
    assertNotNull(id);
    assertThat(LoggedSqlCollector.current()).isEmpty();
    
    EBasicVer b3 = new EBasicVer("b3");
    server.save(b3);
    
  }

  @Test
  public void noflushWhenIdIsLoaded() {

    EbeanServer server = Ebean.getDefaultServer();
    Transaction txn = server.beginTransaction();
    try {
      LoggedSqlCollector.start();
      txn.setBatchMode(true);

      EBasicVer b1 = new EBasicVer("b1");
      b1.setId(546864);
      server.save(b1, txn);

      EBasicVer b2 = new EBasicVer("b2");
      b2.setId(21354);
      server.save(b2, txn);

      assertThat(LoggedSqlCollector.current()).isEmpty();
      //dont flush here
      Integer id = b1.getId();
      assertNotNull(id);
      assertThat(LoggedSqlCollector.current()).isEmpty();

      EBasicVer b3 = new EBasicVer("b3");
      server.save(b3, txn);

      txn.commit();
      assertThat(LoggedSqlCollector.current()).hasSize(3);

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
