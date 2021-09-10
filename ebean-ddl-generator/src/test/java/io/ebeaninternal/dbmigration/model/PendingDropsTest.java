package io.ebeaninternal.dbmigration.model;

import io.ebean.migration.MigrationVersion;
import io.ebeaninternal.dbmigration.migration.ChangeSet;
import io.ebeaninternal.dbmigration.migration.DropColumn;
import io.ebeaninternal.dbmigration.migration.Migration;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class PendingDropsTest {

  static final MigrationVersion V1_1 = MigrationVersion.parse("1.1");
  static final MigrationVersion V1_2 = MigrationVersion.parse("1.2");

  @Test
  public void test_add() {

    PendingDrops pendingDrops = new PendingDrops();
    pendingDrops.add(V1_1, new ChangeSet());
  }

  @Test
  public void test_appliedDropsFor_when_matchesSome_then_removesMatched() {

    PendingDrops pendingDrops = new PendingDrops();

    DropColumn one = col("one");
    DropColumn two = col("two");
    pendingDrops.add(V1_1, changeSet(one, two));
    pendingDrops.add(V1_1, changeSet("three", "four"));
    assertThat(pendingDrops.testGetEntryFor(V1_1).list).asList().hasSize(2);

    ChangeSet applied = changeSet("two");
    applied.setDropsFor("1.1");

    assertThat(pendingDrops.appliedDropsFor(applied)).isFalse();
    assertThat(pendingDrops.testGetEntryFor(V1_1).list).asList().hasSize(2);
    assertThat(pendingDrops.testGetEntryFor(V1_1).list.get(0).getChangeSetChildren()).asList().containsExactly(one);
  }

  @Test
  public void test_appliedDropsFor_when_matchesAll_then_removesChangeSet() {

    PendingDrops pendingDrops = new PendingDrops();

    DropColumn one = col("one");
    DropColumn two = col("two");
    pendingDrops.add(V1_1, changeSet(one, two));
    pendingDrops.add(V1_1, changeSet("three", "four"));
    assertThat(pendingDrops.testGetEntryFor(V1_1).list).asList().hasSize(2);

    ChangeSet applied = changeSet("two", "one");
    applied.setDropsFor("1.1");

    assertThat(pendingDrops.appliedDropsFor(applied)).isFalse();
    assertThat(pendingDrops.testGetEntryFor(V1_1).list).asList().hasSize(1);
  }

  @Test
  public void test_appliedDropsFor_when_changeSetSuppressed_isIgnored() {

    PendingDrops pendingDrops = new PendingDrops();

    DropColumn one = col("one");
    ChangeSet changeSet = changeSet(one);
    changeSet.setSuppressDropsForever(true);
    pendingDrops.add(V1_1, changeSet);

    ChangeSet applied = changeSet("one");
    applied.setDropsFor("1.1");

    assertThat(pendingDrops.appliedDropsFor(applied)).isFalse();
    assertThat(pendingDrops.testGetEntryFor(V1_1).list).asList().hasSize(1);
  }


  @Test
  public void test_pendingDrops() {

    PendingDrops pendingDrops = new PendingDrops();
    assertThat(pendingDrops.pendingDrops()).isEmpty();

    pendingDrops.add(V1_1, new ChangeSet());
    pendingDrops.add(V1_2, new ChangeSet());
    assertThat(pendingDrops.pendingDrops()).containsExactly("1.1", "1.2");
  }

  @Test
  public void test_pendingDrops_when_suppressForever() {

    PendingDrops pendingDrops = new PendingDrops();
    assertThat(pendingDrops.pendingDrops()).isEmpty();

    pendingDrops.add(V1_1, newSuppressForeverChangeSet());
    assertThat(pendingDrops.pendingDrops()).isEmpty();

    pendingDrops.add(V1_2, new ChangeSet());
    assertThat(pendingDrops.pendingDrops()).containsExactly("1.2");
  }

  @Test
  public void test_pendingDrops_when_both() {

    PendingDrops pendingDrops = new PendingDrops();
    assertThat(pendingDrops.pendingDrops()).isEmpty();

    pendingDrops.add(V1_1, newSuppressForeverChangeSet());
    pendingDrops.add(V1_1, new ChangeSet());
    assertThat(pendingDrops.pendingDrops()).containsExactly("1.1");

    pendingDrops.add(V1_2, new ChangeSet());
    assertThat(pendingDrops.pendingDrops()).containsExactly("1.1", "1.2");
  }

  @Test
  public void test_migrationForVersion() {

    PendingDrops pendingDrops = new PendingDrops();

    ChangeSet applyDropChangeSet1 = new ChangeSet();
    ChangeSet applyDropChangeSet2 = new ChangeSet();

    MigrationVersion version = V1_1;
    pendingDrops.add(version, applyDropChangeSet1);
    pendingDrops.add(version, applyDropChangeSet2);

    Migration migration = pendingDrops.migrationForVersion("1_1");
    assertThat(migration.getChangeSet()).containsExactly(applyDropChangeSet1, applyDropChangeSet2);

    assertThat(pendingDrops.testContainsEntryFor(version)).isFalse();
  }

  @Test
  public void test_migrationForVersion_when_both() {

    PendingDrops pendingDrops = new PendingDrops();

    ChangeSet applyDropChangeSet = new ChangeSet();
    MigrationVersion version = V1_1;
    pendingDrops.add(version, newSuppressForeverChangeSet());
    pendingDrops.add(version, applyDropChangeSet);

    Migration migration = pendingDrops.migrationForVersion("1_1");
    assertThat(migration.getChangeSet()).containsExactly(applyDropChangeSet);

    assertThat(pendingDrops.testContainsEntryFor(version)).isTrue();
  }

  @Test
  public void test_migrationForVersion_when_next() {

    PendingDrops pendingDrops = new PendingDrops();
    MigrationVersion version = V1_1;

    ChangeSet applyDropChangeSet = new ChangeSet();
    pendingDrops.add(version, newSuppressForeverChangeSet());
    pendingDrops.add(version, applyDropChangeSet);


    Migration migration = pendingDrops.migrationForVersion("next");
    assertThat(migration.getChangeSet()).containsExactly(applyDropChangeSet);
    assertThat(pendingDrops.testContainsEntryFor(version)).isTrue();
  }

  @Test
  public void test_migrationForVersion_when_next_isSuppressForever() {
    PendingDrops pendingDrops = new PendingDrops();
    pendingDrops.add(V1_1, newSuppressForeverChangeSet());
    assertThrows(IllegalArgumentException.class, () ->  pendingDrops.migrationForVersion("next"));
  }

  @Test
  public void test_migrationForVersion_when_doesNotExist() {
    PendingDrops pendingDrops = new PendingDrops();
    assertThrows(IllegalArgumentException.class, () ->  pendingDrops.migrationForVersion("1_1"));
  }

  @Test
  public void test_migrationForVersion_when_next_doesNotExist(){
    PendingDrops pendingDrops = new PendingDrops();
    assertThrows(IllegalArgumentException.class, () -> pendingDrops.migrationForVersion("next"));
  }


  @Test
  public void test_registerPendingHistoryDropColumns() {
    TDModelContainer modelContainer = new TDModelContainer();

    DropColumn drop1 = col("one");
    drop1.setWithHistory(Boolean.TRUE);

    DropColumn drop2 = col("two");

    ChangeSet changeSet = changeSet(drop1, drop2);

    PendingDrops pendingDrops = new PendingDrops();
    pendingDrops.add(V1_1, changeSet);
    pendingDrops.registerPendingHistoryDropColumns(modelContainer);

    assertThat(modelContainer.drops).containsExactly(changeSet);
  }

  static class TDModelContainer extends ModelContainer {

    List<ChangeSet> drops = new ArrayList<>();

    @Override
    public void registerPendingHistoryDropColumns(ChangeSet changeSet) {
      drops.add(changeSet);
    }
  }


  private ChangeSet newSuppressForeverChangeSet() {
    ChangeSet changeSet = new ChangeSet();
    changeSet.setSuppressDropsForever(Boolean.TRUE);
    return changeSet;
  }

  static ChangeSet changeSet(String... colNames) {
    return EntryTest.changeSet(colNames);
  }

  static ChangeSet changeSet(DropColumn... drops) {
    ChangeSet changeSet = new ChangeSet();
    for (DropColumn dropColumn : drops) {
      changeSet.getChangeSetChildren().add(dropColumn);
    }

    return changeSet;
  }

  static DropColumn col(String colName) {
    return EntryTest.col(colName);
  }

}
