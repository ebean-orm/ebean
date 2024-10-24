package org.tests.basic;

import io.ebean.DB;
import io.ebean.DatabaseFactory;
import io.ebean.QueryIterator;
import io.ebean.DatabaseBuilder;
import io.ebean.config.DatabaseConfig;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TestPersistenceContextMany extends BaseTestCase {


  @Entity
  @Inheritance
  public abstract static class TestModel3 {

    @Id
    private int id;

    @Size(max = 255)
    private String someData;

    @OneToMany(cascade = CascadeType.ALL)
    private List<TestModel3Many1> many1;

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }

    public void setSomeData(String someData) {
      this.someData = someData;
    }

    public String getSomeData() {
      return someData;
    }

    public List<TestModel3Many1> getMany1() {
      return many1;
    }
  }

  @Entity
  @Inheritance
  @DiscriminatorValue("A")
  public static class TestModel3A extends TestModel3 {

  }

  @Entity
  @Inheritance
  @DiscriminatorValue("B")
  public static class TestModel3B extends TestModel3 {
    @OneToMany(cascade = CascadeType.ALL)
    private List<TestModel3Many2> many2;

    public List<TestModel3Many2> getMany2() {
      return many2;
    }
  }

  @Entity
  public static class TestModel3Many1 {
    @Id
    private int id;

    @ManyToOne
    private TestModel3 base;
  }

  @Entity
  public static class TestModel3Many2 {
    @Id
    private int id;

    @ManyToOne
    private TestModel3B base;
  }

  @Test
  @Disabled
  void initDb() {
    DatabaseConfig config = new DatabaseConfig();
    config.setName("h2-batch");
    config.loadFromProperties();
    config.setDdlExtra(false);
    config.getDataSourceConfig().username("sa");
    config.getDataSourceConfig().password("sa");
    config.getDataSourceConfig().url("jdbc:h2:file:./testsFileMany;DB_CLOSE_ON_EXIT=FALSE;NON_KEYWORDS=KEY,VALUE");
    config.addClass(TestModel3.class);
    config.addClass(TestModel3A.class);
    config.addClass(TestModel3B.class);
    config.addClass(TestModel3Many1.class);
    config.addClass(TestModel3Many2.class);
    DatabaseFactory.create(config);

    String base = "x".repeat(240);
    // 10 mio TestModel - each needs about 1/4 kbytes -> 2,5 GB in total
    List<TestModel3> batch = new ArrayList<>();
    for (int i = 0; i < 1_000_000; i++) {
      TestModel3 m;
      if (i == 5) {
        m = new TestModel3A();
      } else {
        m = new TestModel3B();
        ((TestModel3B) m).getMany2().add(new TestModel3Many2());
        ((TestModel3B) m).getMany2().add(new TestModel3Many2());
      }
      m.getMany1().add(new TestModel3Many1());

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
    config.getDataSourceConfig().username("sa");
    config.getDataSourceConfig().password("sa");
    config.getDataSourceConfig().url("jdbc:h2:file:./testsFileMany;DB_CLOSE_ON_EXIT=FALSE;NON_KEYWORDS=KEY,VALUE");
    config.addClass(TestModel3.class);
    config.addClass(TestModel3A.class);
    config.addClass(TestModel3B.class);
    config.addClass(TestModel3Many1.class);
    config.addClass(TestModel3Many2.class);
    DatabaseFactory.create(config);

    AtomicInteger i = new AtomicInteger();
    System.out.println("Doing findEach");
    DB.find(TestModel3.class).select("*").findEach(c -> {
      i.incrementAndGet();
    });
    System.out.println("Read " + i + " entries");

    i.set(0);
    System.out.println("Doing findEach with lazyLoad");
    DB.find(TestModel3.class).select("*").findEach(c -> {
      i.incrementAndGet();
      i.addAndGet(c.getMany1().size());
      if (c instanceof TestModel3B) {
        i.addAndGet(((TestModel3B) c).getMany2().size());
      }
    });
    System.out.println("Read " + i + " entries"); // 3999998 is correct

    i.set(0);
    System.out.println("Doing findStream");
    DB.find(TestModel3.class).select("*").findStream().forEach(c -> i.incrementAndGet());
    System.out.println("Read " + i + " entries");

    i.set(0);
    System.out.println("Doing findIterate");
    QueryIterator<TestModel3> iter = DB.find(TestModel3.class).select("*").findIterate();
    while (iter.hasNext()) {
      iter.next();
      i.incrementAndGet();
    }
    System.out.println("Read " + i + " entries");

    System.out.println("Doing FindList, will hold all entries in memory. Expect OOM with -Xmx100m.");
    List<TestModel3> lst = DB.find(TestModel3.class).select("*").findList();
    System.out.println("Read " + lst.size() + " entries");
  }
}
