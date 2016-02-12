package com.avaje.ebean.dbmigration.model;

import com.avaje.ebean.dbmigration.migration.ChangeSet;
import com.avaje.ebean.dbmigration.migration.DropColumn;
import com.avaje.ebean.dbmigration.migration.Migration;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class PendingDropsTest {

  @Test
  public void testAdd() throws Exception {

    ChangeSet cs = new ChangeSet();

    PendingDrops pendingDrops = new PendingDrops();
    pendingDrops.add(MigrationVersion.parse("1.1"), cs);
  }

  @Test
  public void test_add_appliedDropsFor() throws Exception {

    PendingDrops pendingDrops = new PendingDrops();
    assertThat(pendingDrops.pendingDrops()).isEmpty();

    pendingDrops.add(MigrationVersion.parse("1.1"), new ChangeSet());
    assertThat(pendingDrops.pendingDrops()).containsExactly("1.1");

    pendingDrops.appliedDropsFor(MigrationVersion.parse("1_1"));
    assertThat(pendingDrops.pendingDrops()).isEmpty();
  }

  @Test
  public void test_add_appliedDropsFor_whenSuppressed() throws Exception {

    PendingDrops pendingDrops = new PendingDrops();
    assertThat(pendingDrops.pendingDrops()).isEmpty();

    pendingDrops.add(MigrationVersion.parse("1.1"), newSuppressForeverChangeSet());
    assertThat(pendingDrops.pendingDrops()).isEmpty();

    pendingDrops.appliedDropsFor(MigrationVersion.parse("1_1"));
  }


  @Test
  public void test_add_appliedDropsFor_whenBoth() throws Exception {

    PendingDrops pendingDrops = new PendingDrops();
    assertThat(pendingDrops.pendingDrops()).isEmpty();

    pendingDrops.add(MigrationVersion.parse("1.1"), newSuppressForeverChangeSet());
    pendingDrops.add(MigrationVersion.parse("1.1"), new ChangeSet());
    assertThat(pendingDrops.pendingDrops()).containsExactly("1.1");

    pendingDrops.appliedDropsFor(MigrationVersion.parse("1_1"));
    assertThat(pendingDrops.pendingDrops()).isEmpty();
  }

  @Test
  public void test_pendingDrops() throws Exception {

    PendingDrops pendingDrops = new PendingDrops();
    assertThat(pendingDrops.pendingDrops()).isEmpty();

    pendingDrops.add(MigrationVersion.parse("1.1"), new ChangeSet());
    pendingDrops.add(MigrationVersion.parse("1.2"), new ChangeSet());
    assertThat(pendingDrops.pendingDrops()).containsExactly("1.1", "1.2");
  }

  @Test
  public void test_pendingDrops_when_suppressForever() throws Exception {

    PendingDrops pendingDrops = new PendingDrops();
    assertThat(pendingDrops.pendingDrops()).isEmpty();

    pendingDrops.add(MigrationVersion.parse("1.1"), newSuppressForeverChangeSet());
    assertThat(pendingDrops.pendingDrops()).isEmpty();

    pendingDrops.add(MigrationVersion.parse("1.2"), new ChangeSet());
    assertThat(pendingDrops.pendingDrops()).containsExactly("1.2");
  }

  @Test
  public void test_pendingDrops_when_both() throws Exception {

    PendingDrops pendingDrops = new PendingDrops();
    assertThat(pendingDrops.pendingDrops()).isEmpty();

    pendingDrops.add(MigrationVersion.parse("1.1"), newSuppressForeverChangeSet());
    pendingDrops.add(MigrationVersion.parse("1.1"), new ChangeSet());
    assertThat(pendingDrops.pendingDrops()).containsExactly("1.1");

    pendingDrops.add(MigrationVersion.parse("1.2"), new ChangeSet());
    assertThat(pendingDrops.pendingDrops()).containsExactly("1.1", "1.2");

    pendingDrops.appliedDropsFor(MigrationVersion.parse("1_1"));
    assertThat(pendingDrops.pendingDrops()).containsExactly("1.2");

    pendingDrops.appliedDropsFor(MigrationVersion.parse("1_2"));
    assertThat(pendingDrops.pendingDrops()).isEmpty();
  }

  @Test
  public void testMigrationForVersion() throws Exception {

    PendingDrops pendingDrops = new PendingDrops();

    ChangeSet applyDropChangeSet1 = new ChangeSet();
    ChangeSet applyDropChangeSet2 = new ChangeSet();

    MigrationVersion version = MigrationVersion.parse("1.1");
    pendingDrops.add(version, applyDropChangeSet1);
    pendingDrops.add(version, applyDropChangeSet2);

    Migration migration = pendingDrops.migrationForVersion("1_1");
    assertThat(migration.getChangeSet()).containsExactly(applyDropChangeSet1, applyDropChangeSet2);

    assertThat(pendingDrops.testContainsEntryFor(version)).isFalse();
  }

  @Test
  public void testMigrationForVersion_when_both() throws Exception {

    PendingDrops pendingDrops = new PendingDrops();

    ChangeSet applyDropChangeSet = new ChangeSet();
    MigrationVersion version = MigrationVersion.parse("1.1");
    pendingDrops.add(version, newSuppressForeverChangeSet());
    pendingDrops.add(version, applyDropChangeSet);

    Migration migration = pendingDrops.migrationForVersion("1_1");
    assertThat(migration.getChangeSet()).containsExactly(applyDropChangeSet);

    assertThat(pendingDrops.testContainsEntryFor(version)).isTrue();
  }

  @Test
  public void testMigrationForVersion_when_next() throws Exception {

    PendingDrops pendingDrops = new PendingDrops();
    MigrationVersion version = MigrationVersion.parse("1.1");

    ChangeSet applyDropChangeSet = new ChangeSet();
    pendingDrops.add(version, newSuppressForeverChangeSet());
    pendingDrops.add(version, applyDropChangeSet);


    Migration migration = pendingDrops.migrationForVersion("next");
    assertThat(migration.getChangeSet()).containsExactly(applyDropChangeSet);
    assertThat(pendingDrops.testContainsEntryFor(version)).isTrue();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMigrationForVersion_when_next_isSuppressForever() throws Exception {

    PendingDrops pendingDrops = new PendingDrops();
    pendingDrops.add(MigrationVersion.parse("1.1"), newSuppressForeverChangeSet());
    pendingDrops.migrationForVersion("next");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMigrationForVersion_when_doesNotExist() throws Exception {

    PendingDrops pendingDrops = new PendingDrops();
    pendingDrops.migrationForVersion("1_1");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMigrationForVersion_when_next_doesNotExist() throws Exception {

    PendingDrops pendingDrops = new PendingDrops();
    pendingDrops.migrationForVersion("next");
  }

  private ChangeSet newSuppressForeverChangeSet() {
    ChangeSet changeSet = new ChangeSet();
    changeSet.setSuppressDropsForever(Boolean.TRUE);
    return changeSet;
  }


  @Test
  public void testRegisterPendingHistoryDropColumns() throws Exception {

    TDModelContainer modelContainer = new TDModelContainer();


    DropColumn drop1 = new DropColumn();
    drop1.setWithHistory(Boolean.TRUE);

    DropColumn drop2 = new DropColumn();

    ChangeSet changeSet = new ChangeSet();
    changeSet.getChangeSetChildren().add(drop1);
    changeSet.getChangeSetChildren().add(drop2);

    PendingDrops pendingDrops = new PendingDrops();
    pendingDrops.add(MigrationVersion.parse("1.1"), changeSet);
    pendingDrops.registerPendingHistoryDropColumns(modelContainer);

    assertThat(modelContainer.drops).containsExactly(drop1);
  }

  class TDModelContainer extends ModelContainer {

    List<DropColumn> drops = new ArrayList<DropColumn>();

    @Override
    public void registerPendingDropColumn(DropColumn dropColumn) {
      drops.add(dropColumn);
    }
  }
}