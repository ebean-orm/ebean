package org.tests.query;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import io.ebean.Transaction;
import io.ebean.annotation.Transactional;
import io.ebean.bean.PersistenceContext;
import io.ebean.test.LoggedSql;
import io.ebeaninternal.api.SpiTransaction;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;
import org.tests.model.basic.EBasicLog;
import org.tests.model.basic.ResetBasicData;
import org.tests.o2m.OmBasicChild;
import org.tests.o2m.OmBasicParent;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class TestQueryFindEach extends BaseTestCase {

  private static final Logger log = LoggerFactory.getLogger(TestQueryFindEach.class);
  private final Random random = new Random();
  private final AtomicInteger batchCount = new AtomicInteger();
  private final AtomicInteger rowCount = new AtomicInteger();

  @Test
  public void test() {

    ResetBasicData.reset();

    Query<Customer> query = DB.find(Customer.class)
      .fetchQuery("contacts")
      .where().gt("id", 0).orderBy("id")
      .setMaxRows(2).query();

    final AtomicInteger counter = new AtomicInteger(0);

    query.findEach(customer -> {
      counter.incrementAndGet();
      assertNotNull(customer.getName());
    });

    assertEquals(2, counter.get());
  }

  private void resetFindEachCounts() {
    batchCount.set(0);
    rowCount.set(0);
  }

  private void seedData() {
    for (int i = 0; i < 15; i++) {
      EBasicLog log = new EBasicLog("findEachBatch " + i);
      DB.save(log);
    }
  }

  @Test
  public void findEachBatch() {
    seedData();

    resetFindEachCounts();
    findEachWithBatch(5);
    assertThat(batchCount.get()).isEqualTo(3);
    assertThat(rowCount.get()).isEqualTo(15);

    resetFindEachCounts();
    findEachWithBatch(10);
    assertThat(batchCount.get()).isEqualTo(2);
    assertThat(rowCount.get()).isEqualTo(15);

    resetFindEachCounts();
    findEachWithBatch(14);
    assertThat(batchCount.get()).isEqualTo(2);
    assertThat(rowCount.get()).isEqualTo(15);

    resetFindEachCounts();
    findEachWithBatch(15);
    assertThat(batchCount.get()).isEqualTo(1);
    assertThat(rowCount.get()).isEqualTo(15);

    resetFindEachCounts();
    findEachWithBatch(16);
    assertThat(batchCount.get()).isEqualTo(1);
    assertThat(rowCount.get()).isEqualTo(15);

    resetFindEachCounts();
    findEachWithBatch(20);
    assertThat(batchCount.get()).isEqualTo(1);
    assertThat(rowCount.get()).isEqualTo(15);
  }

  private void findEachWithBatch(int batchSize) {
    DB.find(EBasicLog.class)
      .where().startsWith("name", "findEachBatch")
      .findEach(batchSize, batch -> {
        int batchId = batchCount.incrementAndGet();
        int rows = rowCount.addAndGet(batch.size());
        log.info("batch id:{} size:{} total rows:{}", batchId, batch.size(), rows);
      });
  }

  @Test
  public void persistenceContext_scope() {

    ResetBasicData.reset();

    Query<Contact> query = DB.find(Contact.class);

    try (final Transaction transaction = DB.beginTransaction()) {
      // effectively loads customers into persistence context
      final List<Customer> customerList = DB.find(Customer.class)
        .select("name")
        .findList();

      SpiTransaction spiTxn = (SpiTransaction) transaction;
      PersistenceContext pc = spiTxn.persistenceContext();
      assertThat(pc.size(Customer.class)).isEqualTo(customerList.size());

      LoggedSql.start();
      query.findEach(contact -> {
        // use customer from persistence context (otherwise would invoke lazy loading)
        assertNotNull(contact.getCustomer().getName());
      });

      final List<String> sql = LoggedSql.stop();
      assertThat(sql).hasSize(1);
    }
  }

  /**
   * Test the behaviour when an exception is thrown inside the findVisit().
   */
  @Test
  public void testVisitThrowingException() {
    ResetBasicData.reset();

    Query<Customer> query = DB.find(Customer.class)
      .fetchQuery("contacts")
      .where().gt("id", 0).orderBy("id")
      .setMaxRows(2).query();

    final AtomicInteger counter = new AtomicInteger(0);

    assertThrows(IllegalStateException.class, () ->
      query.findEach(customer -> {
        counter.incrementAndGet();
        if (counter.intValue() > 0) {
          throw new IllegalStateException("cause a failure");
        }
      }));
  }

  @Test
  public void iterateResetLimit() {

    DB.find(OmBasicChild.class).delete();
    DB.find(OmBasicParent.class).delete();
    insertData();

    test_setLazyLoadBatchSize_withFetchLazy();

    LoggedSql.start();

    DB.find(OmBasicChild.class)
      .setLazyLoadBatchSize(100)
      .findEach(child -> {
        assertNotNull(child.getParent().getName());
      });

    final List<String> sql = LoggedSql.stop();
    assertThat(sql.size()).isLessThan(50);
  }

  private void test_setLazyLoadBatchSize_withFetchLazy() {

    LoggedSql.start();

    DB.find(OmBasicParent.class)
      .setLazyLoadBatchSize(5)
      .fetchLazy("children")
      .setMaxRows(50)
      .findEach(it -> {
        it.getChildren().size();
      });

    final List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(11);
    assertThat(sql.get(0)).contains(" from om_basic_parent ");
    for (int i = 1; i < 11; i++) {
      assertThat(sql.get(i)).contains(" --bind(Array[5]");
    }
  }

  @Transactional(batchSize = 40)
  private void insertData() {
    for (int i = 0; i < 150; i++) {
      insertData2(i);
    }
  }

  private void insertData2(int i) {
    OmBasicParent p = new OmBasicParent("bl_" + i);
    DB.save(p);

    int children = 20 + random.nextInt(10);
    for (int j = 0; j < children; j++) {
      OmBasicChild c = new OmBasicChild("bl_" + i + "_" + j, p);
      DB.save(c);
    }
  }
}
