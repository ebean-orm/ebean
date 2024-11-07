package org.tests.batchinsert;

import io.ebean.DB;
import io.ebean.Database;
import io.ebean.Transaction;
import io.ebean.annotation.PersistBatch;
import io.ebean.annotation.Platform;
import io.ebean.annotation.Transactional;
import io.ebean.meta.MetaTimedMetric;
import io.ebean.meta.ServerMetrics;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import io.ebean.xtest.IgnorePlatform;
import io.ebean.xtest.base.DtoQuery2Test;
import io.ebeaninternal.api.SpiTransaction;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.*;
import org.tests.query.cache.Acl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestBatchInsertFlush extends BaseTestCase {

  @Test
  public void batchFlush() {

    TSMaster m = new TSMaster();
    m.setName("master1");
    DB.save(m);
    DB.getDefault().cacheManager().clearAll();

    try (Transaction txn = DB.beginTransaction()) {
      txn.setBatchSize(2);
      txn.setBatchMode(true);

      List<Customer> customers = new ArrayList<>();

      for (int i = 0; i < 3; i++) {
        Customer customer = ResetBasicData.createCustomer("BatchFlushPreInsert " + i, null, null, 3);
        customer.addContact(new Contact("BatchFlush" + i, "Blue"));
        customers.add(customer);
      }

      for (int i = 3; i < 6; i++) {
        Customer customer = ResetBasicData.createCustomer("BatchFlushPostInsert " + i, null, null, 3);
        customer.addContact(new Contact("BatchFlush" + i, "Blue"));
        customers.add(customer);
      }

      DB.saveAll(customers);

      txn.commit();
    } finally {
      DB.find(Customer.class).where().startsWith("name", "BatchFlush").delete();
      DB.find(Contact.class).where().startsWith("firstName", "BatchFlush").startsWith("lastName", "Blue").delete();
    }
  }

  @Test
  public void no_cascade() {

    Database server = DB.getDefault();

    resetAllMetrics();

    Transaction transaction = server.beginTransaction();
    try {
      transaction.setPersistCascade(false);
      transaction.setBatchSize(10);
      transaction.setBatchMode(true);
      transaction.setLabel("TestBatchInsertFlush.no_cascade");

      LoggedSql.start();

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

      List<String> sql = LoggedSql.stop();

      // we get the 2 master inserts first
      assertSql(sql.get(0)).contains("insert into t_atable_thatisrelatively");
      assertSql(sql.get(1)).contains("-- bind(");
      // detail
      assertThat(sql.get(4)).contains("insert into t_detail_with_other_namexxxyy");

      assertThat(((SpiTransaction) transaction).label()).isEqualTo("TestBatchInsertFlush.no_cascade");

    } finally {
      transaction.end();
    }

    ServerMetrics metrics = collectMetrics();
    List<MetaTimedMetric> txnStats = metrics.timedMetrics();
    for (MetaTimedMetric txnMetric : txnStats) {
      System.out.println(txnMetric);
    }
    assertThat(txnStats).hasSize(4);
    assertThat(txnStats.get(0).name()).isEqualTo("txn.main");
    assertThat(txnStats.get(1).name()).isEqualTo("txn.named.TestBatchInsertFlush.no_cascade");
    assertThat(txnStats.get(2).name()).isEqualTo("iud.TSDetail.insertBatch");
    assertThat(txnStats.get(3).name()).isEqualTo("iud.TSMaster.insertBatch");
  }

  @Test
  @Transactional(batch = PersistBatch.ALL, flushOnQuery = false)
  public void transactional_flushOnQueryFalse() {

    LoggedSql.start();

    DB.save(new EBasicVer("b1"));
    DB.save(new EBasicVer("b2"));

    // does not trigger JDBC batch with flushOnQuery = false
    DB.find(Customer.class).findCount();

    List<String> sql = LoggedSql.stop();
    assertSql(sql.get(0)).contains("select count(*)");
  }

  @Test
  @Transactional(batchSize = 20)
  public void transactional_flushOnSqlQuery() {

    LoggedSql.start();

    DB.save(new EBasicVer("b1"));
    DB.save(new EBasicVer("b2"));

    // trigger JDBC batch by default
    DB.sqlQuery("select count(*) from e_basicver")
      .mapToScalar(Integer.class)
      .findOne();

    List<String> sql = LoggedSql.stop();
    assertSql(sql.get(0)).contains("insert into e_basicver");
  }

  @Test
  @Transactional(batchSize = 20)
  public void transactional_flushOnDtoQuery() {

    LoggedSql.start();

    DB.save(new EBasicVer("b1"));
    DB.save(new EBasicVer("b2"));

    // trigger JDBC batch by default
    DB.findDto(DtoQuery2Test.DCust.class, "select id, name from o_customer")
      .findList();

    List<String> sql = LoggedSql.stop();
    assertSql(sql.get(0)).contains("insert into e_basicver");
  }

  @Test
  @Transactional(batch = PersistBatch.ALL)
  public void transactional_flushOnQuery() {

    LoggedSql.start();

    DB.save(new EBasicVer("b1"));
    DB.save(new EBasicVer("b2"));

    // by default triggers flush of JDBC batch
    DB.find(Customer.class).findCount();

    List<String> sql = LoggedSql.stop();
    assertSql(sql.get(0)).contains("insert into e_basicver");
  }

  @Test
  @Transactional(batch = PersistBatch.ALL)
  @IgnorePlatform({Platform.SQLSERVER, Platform.HANA}) // has generated IDs
  public void transactional_flushOnGetId() {

    Database server = DB.getDefault();
    LoggedSql.start();

    EBasicVer b1 = new EBasicVer("b1");
    server.save(b1);

    EBasicVer b2 = new EBasicVer("b2");
    server.save(b2);

    //flush here
    assertThat(LoggedSql.collect()).isEmpty();
    Integer id = b1.getId();
    assertNotNull(id);
    assertThat(LoggedSql.collect()).hasSize(4);

    EBasicVer b3 = new EBasicVer("b3");
    server.save(b3);
  }

  @Test
  @IgnorePlatform({Platform.SQLSERVER, Platform.HANA})
  public void testFlushOnGetId() {

    Database server = DB.getDefault();
    Transaction txn = server.beginTransaction();
    try {
      LoggedSql.start();
      txn.setBatchMode(true);

      EBasicVer b1 = new EBasicVer("b1");
      server.save(b1, txn);

      EBasicVer b2 = new EBasicVer("b2");
      server.save(b2, txn);

      //flush here
      assertThat(LoggedSql.collect()).isEmpty();
      Integer id = b1.getId();
      assertNotNull(id);
      assertThat(LoggedSql.collect()).hasSize(4);

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

    Database server = DB.getDefault();

    LoggedSql.start();

    EBasicVer b1 = new EBasicVer("b1");
    b1.setId(78965);
    server.save(b1);

    EBasicVer b2 = new EBasicVer("b2");
    b2.setId(78645);
    server.save(b2);

    assertThat(LoggedSql.collect()).isEmpty();
    // dont flush here
    Integer id = b1.getId();
    assertNotNull(id);
    assertThat(LoggedSql.collect()).isEmpty();

    EBasicVer b3 = new EBasicVer("b3");
    server.save(b3);

  }

  @Test
  @IgnorePlatform({Platform.SQLSERVER, Platform.ORACLE})
  public void noflushWhenIdIsLoaded() {

    Database server = DB.getDefault();
    Transaction txn = server.beginTransaction();
    try {
      LoggedSql.start();
      txn.setBatchMode(true);

      EBasicVer b1 = new EBasicVer("b1");
      b1.setId(546864);
      server.save(b1, txn);

      EBasicVer b2 = new EBasicVer("b2");
      b2.setId(21354);
      server.save(b2, txn);

      assertThat(LoggedSql.collect()).isEmpty();
      //dont flush here
      Integer id = b1.getId();
      assertNotNull(id);
      assertThat(LoggedSql.collect()).isEmpty();

      EBasicVer b3 = new EBasicVer("b3");
      server.save(b3, txn);

      txn.commit();
      assertThat(LoggedSql.collect()).hasSize(7);

    } finally {
      txn.end();
    }
  }

  @Test
  public void testFlushOnGetProperty() {

    Database server = DB.getDefault();
    Transaction txn = server.beginTransaction();
    try {
      txn.setBatchMode(true);

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

    Database server = DB.getDefault();
    Transaction txn = server.beginTransaction();
    try {
      txn.setBatchMode(true);

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

  @Test
  public void testBatchEscalationInsert() {
    try (Transaction txn = DB.beginTransaction()) {
      assertThat(txn.isBatchMode()).isFalse();
      DB.insertAll(List.of(new Acl("test")));
      assertThat(txn.isBatchMode()).isFalse();
    }
  }

  @Test
  public void testBatchEscalationSave() {
    try (Transaction txn = DB.beginTransaction()) {
      assertThat(txn.isBatchMode()).isFalse();
      DB.saveAll(List.of(new Acl("test")));
      assertThat(txn.isBatchMode()).isFalse();
    }
  }
}
