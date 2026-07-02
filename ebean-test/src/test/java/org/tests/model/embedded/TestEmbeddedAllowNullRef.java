package org.tests.model.embedded;

import io.ebean.DB;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for @Embedded(nullable = false) — the embedded field
 * must never be null after loading, even when all embedded columns are null in the DB.
 */
class TestEmbeddedAllowNullRef extends BaseTestCase {

  @Test
  void embeddedIsNeverNull_whenAllColumnsAreNull() {
    EPersonAllowNullRef person = new EPersonAllowNullRef();
    person.setName("Alice");
    // address intentionally left null — all address columns will be null in the DB
    DB.save(person);

    EPersonAllowNullRef loaded = DB.find(EPersonAllowNullRef.class, person.getId());

    assertThat(loaded).isNotNull();
    // with nullable = false, the address reference must never be null
    assertThat(loaded.getAddress())
      .describedAs("address should be a non-null empty instance, not null")
      .isNotNull();
    // all fields inside the embedded bean are still null
    assertThat(loaded.getAddress().getCity()).isNull();

    DB.delete(loaded);
  }

  @Test
  void embeddedIsPopulated_whenColumnsAreSet() {
    EPersonAllowNullRef person = new EPersonAllowNullRef();
    person.setName("Bob");
    EAddress addr = new EAddress();
    addr.setCity("Wellington");
    person.setAddress(addr);
    DB.save(person);

    EPersonAllowNullRef loaded = DB.find(EPersonAllowNullRef.class, person.getId());

    assertThat(loaded.getAddress()).isNotNull();
    assertThat(loaded.getAddress().getCity()).isEqualTo("Wellington");

    DB.delete(loaded);
  }

  @Test
  void noDirtyUpdate_whenLoadedWithNullEmbeddedAndNotModified() {
    EPersonAllowNullRef person = new EPersonAllowNullRef();
    person.setName("Charlie");
    DB.save(person);

    EPersonAllowNullRef loaded = DB.find(EPersonAllowNullRef.class, person.getId());
    // just reading the address reference (which is a non-null empty bean) must not mark dirty
    EAddress address = loaded.getAddress();
    assertThat(address).isNotNull();

    // update should be skipped — nothing changed
    io.ebean.test.LoggedSql.start();
    DB.update(loaded);
    java.util.List<String> sqls = io.ebean.test.LoggedSql.stop();
    assertThat(sqls)
      .describedAs("no UPDATE should be issued when nothing was changed")
      .isEmpty();

    DB.delete(loaded);
  }
}
