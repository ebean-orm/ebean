package org.tests.insert;

import io.ebean.DB;
import io.ebean.Transaction;
import io.ebean.plugin.Property;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.EBasicWithUniqueCon;
import org.tests.model.draftable.Document;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class TestInsertCheckUnique extends BaseTestCase {

  @BeforeEach
  public void clearDb() {
    DB.find(Document.class).asDraft().where().contains("title", "UniqueKey").delete();
    DB.find(Document.class).asDraft().where().isNull("title").delete();
  }

  @Test
  public void insert_duplicateKey() {

    try (Transaction transaction = DB.beginTransaction()) {
      Document doc1 = new Document();
      doc1.setTitle("AUniqueKey_duplicateCheck");
      doc1.setBody("one");

      assertThat(DB.checkUniqueness(doc1)).isEmpty();
      doc1.save();

      Document doc2 = new Document();
      doc2.setTitle("AUniqueKey_duplicateCheck");
      doc2.setBody("clashes with doc1");

      assertThat(DB.checkUniqueness(doc2)).isEmpty();

      DB.getDefault().publish(doc1.getClass(), doc1.getId());

      assertThat(DB.checkUniqueness(doc2).toString()).contains("title");
    }
  }

  @Test
  public void insert_duplicateNull() {
    try (Transaction transaction = DB.beginTransaction()) {
      Document doc1 = new Document();
      doc1.setTitle(null);
      doc1.setBody("one");

      assertThat(DB.checkUniqueness(doc1)).isEmpty();
      doc1.save();

      Document doc2 = new Document();
      doc2.setTitle(null);
      doc2.setBody("clashes with doc1");

      assertThat(DB.checkUniqueness(doc2)).isEmpty();

      DB.getDefault().publish(doc1.getClass(), doc1.getId());

      assertThat(DB.checkUniqueness(doc2)).isEmpty();

      doc2.save();
      DB.getDefault().publish(doc2.getClass(), doc2.getId());
    }
  }

  @Test
  public void example() {

    try (Transaction transaction = DB.beginTransaction()) {
      Document doc1 = new Document();
      doc1.setTitle("One flew over the cuckoo's nest");
      doc1.setBody("one");
      doc1.save();

      DB.getDefault().publish(doc1.getClass(), doc1.getId());

      Document doc2 = new Document();
      doc2.setTitle("One flew over the cuckoo's nest");
      doc2.setBody("clashes with doc1");

      Set<Property> properties = DB.checkUniqueness(doc2);
      if (properties.isEmpty()) {
        // it is unique ... carry on
      } else {
        // build a user friendly message
        // to return message back to user

        String uniqueProperties = properties.toString();

        StringBuilder msg = new StringBuilder();

        properties.forEach((it) -> {
          Object propertyValue = it.value(doc2);
          String propertyName = it.name();
          msg.append(" property[" + propertyName + "] value[" + propertyValue + "]");
        });

        System.out.println("uniqueProperties > " + uniqueProperties);
        System.out.println("      custom msg > " + msg);

      }

      LoggedSql.start();
      assertThat(DB.checkUniqueness(doc2).toString()).contains("title");
      List<String> sql = LoggedSql.stop();
      assertThat(sql).hasSize(1);
      assertThat(sql.get(0)).contains("select").contains(" t0.id from document t0 where t0.title = ?");


    }
  }

  /**
   * When invoking checkUniqueness multiple times, we can benefit from the "exists" query cache if bean has query cache enabled
   */
  @Test
  public void testUseQueryCache() {
    DB.find(EBasicWithUniqueCon.class).delete(); // clean up DB (otherwise test may be affected by other test)

    EBasicWithUniqueCon basic = new EBasicWithUniqueCon();
    basic.setName("foo");
    basic.setOther("bar");
    basic.setOtherOne("baz");

    // create a new bean
    LoggedSql.start();
    assertThat(DB.checkUniqueness(basic, null, true, false)).isEmpty();
    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("select").contains("t0.id from e_basicverucon t0 where t0.name = ?");
    assertThat(sql.get(1)).contains("select").contains("t0.id from e_basicverucon t0 where t0.other = ? and t0.other_one = ?");
    DB.save(basic);
    try {
      // reload from database
      basic = DB.find(EBasicWithUniqueCon.class, basic.getId());

      // and check again
      LoggedSql.start();
      assertThat(DB.checkUniqueness(basic, null, true, false)).isEmpty();
      sql = LoggedSql.stop();
      assertThat(sql).hasSize(2);
      assertThat(sql.get(0)).contains("select").contains("t0.id from e_basicverucon t0 where t0.id <> ? and t0.name = ?");
      assertThat(sql.get(1)).contains("select").contains("t0.id from e_basicverucon t0 where t0.id <> ? and t0.other = ? and t0.other_one = ?");

      // and check again - expect to hit query cache
      LoggedSql.start();
      assertThat(DB.checkUniqueness(basic, null, true, false)).isEmpty();
      sql = LoggedSql.stop();
      assertThat(sql).as("Expected to hit query cache").hasSize(0);

      // and check again, where only one value is changed
      basic.setOther("fooo");
      LoggedSql.start();
      assertThat(DB.checkUniqueness(basic, null, true, false)).isEmpty();
      sql = LoggedSql.stop();
      assertThat(sql).hasSize(1);
      assertThat(sql.get(0)).contains("fooo,baz)");

    } finally {
      DB.delete(EBasicWithUniqueCon.class, basic.getId());
    }
  }


  /**
   * When invoking checkUniqueness multiple times, we can benefit from the "exists" query cache if bean has query cache enabled
   */
  @Test
  public void testSkipClean() {
    DB.find(EBasicWithUniqueCon.class).delete(); // clean up DB (otherwise test may be affected by other test)

    EBasicWithUniqueCon basic = new EBasicWithUniqueCon();
    basic.setName("foo");
    basic.setOther("bar");
    basic.setOtherOne("baz");

    // create a new bean
    LoggedSql.start();
    assertThat(DB.checkUniqueness(basic, null, false, true)).isEmpty();
    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("select").contains("t0.id from e_basicverucon t0 where t0.name = ?");
    assertThat(sql.get(1)).contains("select").contains("t0.id from e_basicverucon t0 where t0.other = ? and t0.other_one = ?");
    DB.save(basic);
    try (Transaction txn = DB.beginTransaction()) {
      // reload from database
      basic = DB.find(EBasicWithUniqueCon.class, basic.getId());

      // and check again. We do not check unmodified properties
      LoggedSql.start();
      assertThat(DB.checkUniqueness(basic, txn, false, true)).isEmpty();
      sql = LoggedSql.stop();
      assertThat(sql).hasSize(0);

      // and check again, where only one value is changed
      basic.setOther("fooo");
      LoggedSql.start();
      assertThat(DB.checkUniqueness(basic, txn, false, true)).isEmpty();
      sql = LoggedSql.stop();
      assertThat(sql).hasSize(1);
      assertThat(sql.get(0)).contains("fooo,baz)");

      // multiple checks will hit DB
      LoggedSql.start();
      assertThat(DB.checkUniqueness(basic, txn, false, true)).isEmpty();
      sql = LoggedSql.stop();
      assertThat(sql).hasSize(1);

      // enable also query cache
      assertThat(DB.checkUniqueness(basic, txn, true, true)).isEmpty();
      LoggedSql.start();
      assertThat(DB.checkUniqueness(basic, txn, true, true)).isEmpty();
      sql = LoggedSql.stop();
      assertThat(sql).isEmpty();
    } finally {
      DB.delete(EBasicWithUniqueCon.class, basic.getId());
    }
  }
}
