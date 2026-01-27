package org.tests.basic;

import io.ebean.DB;
import io.ebean.DatabaseFactory;
import io.ebean.QueryIterator;
import io.ebean.Transaction;
import io.ebean.DatabaseBuilder;
import io.ebean.bean.PersistenceContext;
import io.ebean.config.DatabaseConfig;
import io.ebean.xtest.BaseTestCase;
import io.ebeaninternal.api.SpiPersistenceContext;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.transaction.DefaultPersistenceContext;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.ContactNote;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import javax.validation.constraints.Size;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class TestPersistenceContext extends BaseTestCase {

  @Test
  void testReload() {
    ResetBasicData.reset();
    try (Transaction txn = DB.beginTransaction()) {
      List<ContactNote> notes = new ArrayList<>();
      DB.find(ContactNote.class).findEach(notes::add);
      notes.get(0).setTitle("FooBar");
      DB.find(ContactNote.class).findList();
      assertThat(notes.get(0).getTitle()).isEqualTo("FooBar");
    }
  }

  @Test
  void test() {
    ResetBasicData.reset();

    // implicit transaction with its own persistence context
    Order oBefore = DB.find(Order.class, 1);
    // start a persistence context

    Order order;
    try (Transaction txn = DB.beginTransaction()) {
      order = DB.find(Order.class, 1);
      // not the same instance ...as a different persistence context
      assertNotSame(order, oBefore);

      // finds an existing bean in the persistence context
      // ... so doesn't even execute a query
      Order o2 = DB.find(Order.class, 1);
      Order o3 = DB.reference(Order.class, 1);

      // all the same instance
      assertSame(order, o2);
      assertSame(order, o3);
    }

    // implicit transaction with its own persistence context
    Order oAfter = DB.find(Order.class, 1);
    assertNotSame(oAfter, oBefore);
    assertNotSame(oAfter, order);

    // start a persistence context
    try (Transaction txn = DB.beginTransaction()) {
      Order testOrder = ResetBasicData.createOrderCustAndOrder("testPC");
      Integer id = testOrder.getCustomer().getId();
      Integer orderId = testOrder.getId();

      Customer customer = DB.find(Customer.class)
        .setUseCache(false)
        .setId(id)
        .findOne();
      assert customer != null;

      System.gc();
      Order order2 = DB.find(Order.class, orderId);
      assert order2 != null;
      Customer customer2 = order2.getCustomer();
      assert customer2 != null;

      assertEquals(customer.getId(), customer2.getId());
      assertSame(customer, customer2);
    }
  }

  @Disabled
  @Test
  void findWithGcTest() {
    for (int j = 0; j < 20; j++) {
      for (int i = 0; i < 500; i++) {
        Customer c = new Customer();
        c.setName("Customer #" + i);
        DB.save(c);
      }
      int customerCount = DB.find(Customer.class).findCount();
      AtomicInteger count = new AtomicInteger(customerCount);

      WeakHashMap<Customer, Integer> customers = new WeakHashMap<>();

      DB.find(Customer.class).fetch("orders").findEach(customer -> {
        customers.put(customer, customer.getId());
        if (count.decrementAndGet() == 0) {
          // Trigger garbage collection on last iteration and check if beans disappear from memory
          System.gc();
          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
          }
          customers.size(); // expunge stale entries
          System.out.println("Total instances: " + customerCount + ", instances left in memory: " + customers.size());
        }
      });
    }
  }

  @Disabled // run manually
  @Test
  void testPcScopes_with_weakReferences() throws InterruptedException {
    for (int i = 0; i < 5000; i++) {
      Customer c = new Customer();
      c.setName("Customer #" + i);
      DB.save(c);
      Order o = new Order();
      o.setCustomer(c);
      DB.save(o);
    }

    try (Transaction txn = DB.beginTransaction()) {
      List<Customer> first100 = DB.find(Customer.class).where().le("id", 100).findList();
      assertEquals(100, first100.size());
      for (Customer c : first100) {
        c.setSmallnote("one of the first 100");
      }

      // use lastBean to hold onto a weak reference bean
      Customer[] lastBean = new Customer[1];
      // findEach switches on use of weak reference in persistence context
      DB.find(Customer.class).setLazyLoadBatchSize(1).findEach(customer -> {
        if (customer.getId() <= 100) {
          assertEquals("one of the first 100", customer.getSmallnote());
          // nested finds
          DB.find(Order.class).where().eq("customer", customer).findEach(20, consumer -> {
          });
        } else {
          assertNotEquals("one of the first 100", customer.getSmallnote());
        }
        lastBean[0] = customer;
      });

      SpiPersistenceContext pc = ((SpiTransaction) txn).persistenceContext();
      // the first 100 customers using strong references
      assertThat(pc.toString()).contains("Customer=size:5000 (4900 weak)");
      assertThat(pc.toString()).contains("Order=size:100 (100 weak)");

      System.gc();
      Thread.sleep(100);
      pc.get(Customer.class, 1); // trigger expungeStaleEntries
      // pc.get(Order.class, 1);
      assertThat(pc.toString()).contains("Customer=size:101 (1 weak)");
      assertThat(pc.toString()).contains("Order=size:0 (0 weak)");

      first100 = DB.find(Customer.class).where().le("id", 100).findList();
      for (Customer c : first100) {
        assertEquals("one of the first 100", c.getSmallnote());
      }
      Customer lastBeanFromDb = DB.find(Customer.class).setId(lastBean[0].getId()).findOne();
      assertSame(lastBeanFromDb, lastBean[0]);

      // read 200
      DB.find(Customer.class).where().le("id", 200).findList();
      assertThat(pc.toString()).contains("Customer=size:201 (1 weak)");

      lastBean[0] = null; // allow GC on this one
      lastBeanFromDb = null;
      System.gc();
      Thread.sleep(100);
      assertThat(pc.toString()).contains("Customer=size:200 (0 weak)");
    }
  }

  @Embeddable
  public static class TmId {
    private final UUID id1;
    private final UUID id2;

    public TmId(UUID id1, UUID id2) {
      this.id1 = id1;
      this.id2 = id2;
    }

    public UUID getId1() {
      return id1;
    }

    public UUID getId2() {
      return id2;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      TmId tmId = (TmId) o;
      return Objects.equals(id1, tmId.id1) && Objects.equals(id2, tmId.id2);
    }

    @Override
    public int hashCode() {
      return Objects.hash(id1, id2);
    }
  }

  @Entity
  // @Cache(enableQueryCache = true, enableBeanCache = false)
  public static class TestModel2 {

    @EmbeddedId
    private TmId id;
    @Size(max = 255)
    private String someData;

    public String getSomeData() {
      return someData;
    }

    public void setSomeData(String someData) {
      this.someData = someData;
    }

    public TmId getId() {
      return id;
    }

    public void setId(TmId id) {
      this.id = id;
    }
  }

  @Test
  @Disabled
  void initDb() {
    DatabaseConfig config = new DatabaseConfig();
    config.setName("h2-batch");
    config.loadFromProperties();
    config.setDdlExtra(false);
    config.getDataSourceConfig().setUsername("sa");
    config.getDataSourceConfig().setPassword("sa");
    config.getDataSourceConfig().setUrl("jdbc:h2:file:./testsFile3;DB_CLOSE_ON_EXIT=FALSE;NON_KEYWORDS=KEY,VALUE");
    config.addClass(TestModel2.class);
    config.addClass(TmId.class);
    DatabaseFactory.create(config);

    String base = "x".repeat(240);
    // 10 mio TestModel - each needs about 1/4 kbytes -> 2,5 GB in total
    List<TestModel2> batch = new ArrayList<>();
    for (int i = 0; i < 1_000_000; i++) {
      TestModel2 m = new TestModel2();
      TmId id = new TmId(UUID.randomUUID(), UUID.randomUUID());
      m.setId(id);
      m.setSomeData(base + i); // ensure we have not duplicates
      batch.add(m);
      if (i % 1000 == 0) {
        DB.saveAll(batch);
        batch.clear();
      }
      if (i % 100000 == 0) {
        System.out.println(i);
      }
    }
    DB.saveAll(batch);
  }

  @Test
  @Disabled
  void testFindEachFindList() {
    DatabaseConfig config = new DatabaseConfig();
    config.setName("h2-batch");
    config.loadFromProperties();
    config.setDdlRun(false);
    config.getDataSourceConfig().setUsername("sa");
    config.getDataSourceConfig().setPassword("sa");
    config.getDataSourceConfig().setUrl("jdbc:h2:file:./testsFile3;DB_CLOSE_ON_EXIT=FALSE;NON_KEYWORDS=KEY,VALUE");
    config.addClass(TestModel2.class);
    config.addClass(TmId.class);
    DatabaseFactory.create(config);

    AtomicInteger i = new AtomicInteger();
    System.out.println("Doing findEach");
    DB.find(TestModel2.class).select("*").findEach(c -> {
      i.incrementAndGet();
    });
    System.out.println("Read " + i + " entries");

    i.set(0);
    System.out.println("Doing findStream");
    DB.find(TestModel2.class).select("*").findStream().forEach(c -> i.incrementAndGet());
    System.out.println("Read " + i + " entries");

    i.set(0);
    System.out.println("Doing findIterate");
    QueryIterator<TestModel2> iter = DB.find(TestModel2.class).select("*").findIterate();
    while (iter.hasNext()) {
      iter.next();
      i.incrementAndGet();
    }
    System.out.println("Read " + i + " entries");

    System.out.println("Doing FindList, will hold all entries in memory. Expect OOM with -Xmx100m.");
    List<TestModel2> lst = DB.find(TestModel2.class).select("*").findList();
    System.out.println("Read " + lst.size() + " entries");
  }

  @Test
  void testFindReferenceBean() {

    ResetBasicData.reset();

    BeanDescriptor<Customer> desc = (BeanDescriptor<Customer>) DB.getDefault().pluginApi().beanType(Customer.class);

    // Fill bean cache
    DB.find(Customer.class).setId(1).findOne();

    PersistenceContext pc = new DefaultPersistenceContext();
    desc.findReferenceBean(1, pc);

    assertThat(pc.get(Customer.class, 1)).isNotNull();
  }
}
