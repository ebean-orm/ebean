package io.ebeaninternal.dbmigration.model;

import io.ebeaninternal.dbmigration.migration.ChangeSet;
import io.ebeaninternal.dbmigration.migration.DropColumn;
import org.junit.Test;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class EntryTest {


  @Test
  public void test_when_empty() throws Exception {

    PendingDrops.Entry entry = createEntry();
    assertThat(entry.hasPendingDrops()).isFalse();
  }

  @Test
  public void test_when_normal() throws Exception {

    PendingDrops.Entry entry = createEntry(new ChangeSet());

    assertThat(entry.hasPendingDrops()).isTrue();
  }

  @Test
  public void test_when_suppressOnly() throws Exception {

    ChangeSet cs = new ChangeSet();
    cs.setSuppressDropsForever(Boolean.TRUE);

    PendingDrops.Entry entry = createEntry(cs);

    assertThat(entry.hasPendingDrops()).isFalse();
  }

  @Test
  public void test_when_both() throws Exception {

    ChangeSet cs = new ChangeSet();
    cs.setSuppressDropsForever(Boolean.TRUE);

    PendingDrops.Entry entry = createEntry(cs, new ChangeSet());

    assertThat(entry.hasPendingDrops()).isTrue();
  }

  @Test
  public void test_containsSuppressForever_when_empty() {

    PendingDrops.Entry entry = createEntry();

    assertThat(entry.containsSuppressForever()).isFalse();
  }

  @Test
  public void test_containsSuppressForever_when_notSuppress() {

    PendingDrops.Entry entry = createEntry(new ChangeSet());

    assertThat(entry.containsSuppressForever()).isFalse();
  }

  @Test
  public void test_containsSuppressForever_when_suppress() {

    ChangeSet cs = new ChangeSet();
    cs.setSuppressDropsForever(Boolean.TRUE);
    PendingDrops.Entry entry = createEntry(cs);

    assertThat(entry.containsSuppressForever()).isTrue();
  }

  @Test
  public void test_containsSuppressForever_when_mixed() {

    ChangeSet cs = new ChangeSet();
    cs.setSuppressDropsForever(Boolean.TRUE);

    PendingDrops.Entry entry = createEntry(cs, new ChangeSet());

    assertThat(entry.containsSuppressForever()).isTrue();
  }

  @Test
  public void test_removeDrops_when_columnsMatch() {

    ChangeSet pending = changeSet("one", "two");

    PendingDrops.Entry entry = createEntry(pending);

    assertThat(entry.removeDrops(changeSet("one", "two"))).isTrue();
    assertThat(entry.list).asList().doesNotContain(pending);
  }

  @Test
  public void test_removeDrops_when_subset() {

    DropColumn dropColumnTwo = col("two");
    ChangeSet pending = changeSet("one");
    pending.getChangeSetChildren().add(dropColumnTwo);

    PendingDrops.Entry entry = createEntry(pending);

    assertThat(entry.removeDrops(changeSet("one"))).isFalse();
    assertThat(entry.list).asList().containsExactly(pending);
    assertThat(pending.getChangeSetChildren()).asList().containsExactly(dropColumnTwo);
  }

  @Test
  public void test_removeDrops_when_columnsMatch_butSuppressed() {

    ChangeSet pending = changeSet("one", "two");
    pending.setSuppressDropsForever(Boolean.TRUE);

    PendingDrops.Entry entry = createEntry(pending);

    assertThat(entry.removeDrops(changeSet("one", "two"))).isFalse();
    assertThat(entry.list).asList().contains(pending);
    assertThat(pending.getChangeSetChildren()).asList().hasSize(2);

  }

  static ChangeSet changeSet(String... colName) {

    ChangeSet cs = new ChangeSet();
    for (String col : colName) {
      cs.getChangeSetChildren().add(col(col));
    }
    return cs;
  }

  static DropColumn col(String colName) {
    DropColumn drop = new DropColumn();
    drop.setColumnName(colName);
    drop.setTableName("tab");
    return drop;
  }

  static PendingDrops.Entry createEntry(ChangeSet... pending) {

    PendingDrops.Entry entry = new PendingDrops.Entry(MigrationVersion.parse("1.1"));
    for (ChangeSet changeSet : pending) {
      entry.add(changeSet);
    }
    return entry;
  }
}
