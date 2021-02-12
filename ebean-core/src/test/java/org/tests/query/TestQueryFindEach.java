package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.FetchConfig;
import io.ebean.Query;
import io.ebean.Transaction;
import io.ebean.annotation.Transactional;
import io.ebean.bean.PersistenceContext;
import io.ebeaninternal.api.SpiTransaction;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;
import org.tests.o2m.OmBasicChild;
import org.tests.o2m.OmBasicParent;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class TestQueryFindEach extends BaseTestCase {

  private final Random random = new Random();

  @Test
  public void test() {

    ResetBasicData.reset();

    Query<Customer> query = DB.find(Customer.class)
      .fetchQuery("contacts")
      .where().gt("id", 0).order("id")
      .setMaxRows(2).query();

    final AtomicInteger counter = new AtomicInteger(0);

    query.findEach(customer -> {
      counter.incrementAndGet();
      assertNotNull(customer.getName());
    });

    assertEquals(2, counter.get());
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
      PersistenceContext pc = spiTxn.getPersistenceContext();
      assertThat(pc.size(Customer.class)).isEqualTo(customerList.size());

      LoggedSqlCollector.start();
      query.findEach(contact -> {
        // use customer from persistence context (otherwise would invoke lazy loading)
        assertNotNull(contact.getCustomer().getName());
      });

      final List<String> sql = LoggedSqlCollector.stop();
      assertThat(sql).hasSize(1);
    }
  }

  /**
   * Test the behaviour when an exception is thrown inside the findVisit().
   */
  @Test(expected = IllegalStateException.class)
  public void testVisitThrowingException() {

    ResetBasicData.reset();

    Query<Customer> query = DB.find(Customer.class)
      .fetchQuery("contacts")
      .where().gt("id", 0).order("id")
      .setMaxRows(2).query();

    final AtomicInteger counter = new AtomicInteger(0);

    query.findEach(customer -> {
      counter.incrementAndGet();
      if (counter.intValue() > 0) {
        throw new IllegalStateException("cause a failure");
      }
    });

    fail("Never get here - exception thrown");
  }

  @Test
  public void iterateResetLimit() {

    DB.find(OmBasicChild.class).delete();
    DB.find(OmBasicParent.class).delete();
    insertData();

    LoggedSqlCollector.start();
    try (final Transaction transaction = DB.beginTransaction()) {
      // DB.find(OmBasicParent.class).findList();
      DB.find(OmBasicChild.class)
        .setLazyLoadBatchSize(100)
        //.fetchQuery("parent","name")
        //.fetch("parent","name")
        .findEach(child -> {
          assertNotNull(child.getParent().getName());
        });
    }

    final List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql.size()).isLessThan(50);
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
