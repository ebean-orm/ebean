package io.ebeaninternal.dbmigration.model;

import io.ebeaninternal.dbmigration.migration.ChangeSet;
import io.ebeaninternal.dbmigration.migration.ChangeSetType;
import io.ebeaninternal.dbmigration.migration.DropColumn;
import io.ebeaninternal.dbmigration.migration.DropHistoryTable;
import io.ebeaninternal.dbmigration.migration.DropTable;
import io.ebeaninternal.dbmigration.migration.Migration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * The migrations with pending un-applied drops.
 */
public class PendingDrops {

  private final LinkedHashMap<String, Entry> map = new LinkedHashMap<>();

  /**
   * Add a 'pending drops' changeSet for the given version.
   */
  public void add(MigrationVersion version, ChangeSet changeSet) {

    Entry entry = map.computeIfAbsent(version.normalised(), k -> new Entry(version));
    entry.add(changeSet);
  }

  /**
   * Return the list of versions with pending drops.
   */
  public List<String> pendingDrops() {

    List<String> versions = new ArrayList<>();
    for (Entry value : map.values()) {
      if (value.hasPendingDrops()) {
        versions.add(value.version.asString());
      }
    }
    return versions;
  }

  /**
   * All the pending drops for this migration version have been applied so we need
   * to remove the (unsuppressed) pending drops for this version.
   */
  public boolean appliedDropsFor(ChangeSet changeSet) {

    MigrationVersion version = MigrationVersion.parse(changeSet.getDropsFor());

    Entry entry = map.get(version.normalised());
    if (entry.removeDrops(changeSet)) {
      // it had no suppressForever changeSets so remove completely
      map.remove(version.normalised());
      return true;
    }

    return false;
  }

  /**
   * Return the migration for the pending drops from a version.
   * <p>
   * The value of version can be "next" to find the first un-applied pending drops.
   * </p>
   */
  public Migration migrationForVersion(String pendingVersion) {

    Entry entry = getEntry(pendingVersion);

    Migration migration = new Migration();
    Iterator<ChangeSet> it = entry.list.iterator();
    while (it.hasNext()) {
      ChangeSet changeSet = it.next();
      if (!isSuppressForever(changeSet)) {
        it.remove();
        changeSet.setType(ChangeSetType.APPLY);
        changeSet.setDropsFor(entry.version.asString());
        migration.getChangeSet().add(changeSet);
      }
    }

    if (migration.getChangeSet().isEmpty()) {
      throw new IllegalArgumentException("The remaining pendingDrops changeSets in migration [" + pendingVersion + "] are suppressDropsForever=true and can't be applied");
    }

    if (!entry.containsSuppressForever()) {
      // we can remove it completely as it has no suppressForever changes
      map.remove(entry.version.normalised());
    }

    return migration;
  }

  private Entry getEntry(String pendingVersion) {

    if ("next".equalsIgnoreCase(pendingVersion)) {
      Iterator<Entry> it = map.values().iterator();
      if (it.hasNext()) {
        return it.next();
      }
    } else {
      Entry remove = map.get(MigrationVersion.parse(pendingVersion).normalised());
      if (remove != null) {
        return remove;
      }
    }
    throw new IllegalArgumentException("No 'pendingDrops' changeSets for migration version [" + pendingVersion + "] found");
  }

  /**
   * Register pending drop columns on history tables to the new model.
   */
  public void registerPendingHistoryDropColumns(ModelContainer newModel) {

    for (Entry entry : map.values()) {
      for (ChangeSet changeSet : entry.list) {
        newModel.registerPendingHistoryDropColumns(changeSet);
      }
    }
  }

  /**
   * Return true if there is an Entry for the given version.
   */
  boolean testContainsEntryFor(MigrationVersion version) {
    return map.containsKey(version.normalised());
  }

  /**
   * Return the Entry for the given version.
   */
  Entry testGetEntryFor(MigrationVersion version) {
    return map.get(version.normalised());
  }

  static class Entry {

    final MigrationVersion version;

    final List<ChangeSet> list = new ArrayList<>();

    Entry(MigrationVersion version) {
      this.version = version;
    }

    void add(ChangeSet changeSet) {
      list.add(changeSet);
    }

    /**
     * Return true if this contains suppressForever changeSets.
     */
    boolean containsSuppressForever() {
      for (ChangeSet changeSet : list) {
        if (isSuppressForever(changeSet)) {
          return true;
        }
      }
      return false;
    }

    /**
     * Return true if this contains drops that can be applied / migrated.
     */
    boolean hasPendingDrops() {
      for (ChangeSet changeSet : list) {
        if (!isSuppressForever(changeSet)) {
          return true;
        }
      }
      return false;
    }

    /**
     * Remove the drops that are not suppressForever and return true if that
     * removed all the changeSets (and there are no suppressForever ones).
     */
    boolean removeDrops(ChangeSet appliedDrops) {

      Iterator<ChangeSet> iterator = list.iterator();
      while (iterator.hasNext()) {
        ChangeSet next = iterator.next();
        if (!isSuppressForever(next)) {
          removeMatchingChanges(next, appliedDrops);
          if (next.getChangeSetChildren().isEmpty()) {
            iterator.remove();
          }
        }
      }

      return list.isEmpty();
    }

    /**
     * Remove the applied drops from the pending ones matching by table name and column name.
     */
    private void removeMatchingChanges(ChangeSet pendingDrops, ChangeSet appliedDrops) {

      List<Object> pending = pendingDrops.getChangeSetChildren();
      Iterator<Object> iterator = pending.iterator();
      while (iterator.hasNext()) {
        Object pendingDrop = iterator.next();
        if (pendingDrop instanceof DropColumn && dropColumnIn((DropColumn) pendingDrop, appliedDrops)) {
          iterator.remove();

        } else if (pendingDrop instanceof DropTable && dropTableIn((DropTable) pendingDrop, appliedDrops)) {
          iterator.remove();

        } else if (pendingDrop instanceof DropHistoryTable && dropHistoryTableIn((DropHistoryTable) pendingDrop, appliedDrops)) {
          iterator.remove();

        }
      }
    }

    /**
     * Return true if the pendingDrop is contained in the appliedDrops.
     */
    private boolean dropHistoryTableIn(DropHistoryTable pendingDrop, ChangeSet appliedDrops) {
      for (Object o : appliedDrops.getChangeSetChildren()) {
        if (o instanceof DropHistoryTable && sameHistoryTable(pendingDrop, (DropHistoryTable) o)) {
          return true;
        }
      }
      return false;
    }

    /**
     * Return true if the pendingDrop is contained in the appliedDrops.
     */
    private boolean dropTableIn(DropTable pendingDrop, ChangeSet appliedDrops) {
      for (Object o : appliedDrops.getChangeSetChildren()) {
        if (o instanceof DropTable && sameTable(pendingDrop, (DropTable) o)) {
          return true;
        }
      }
      return false;
    }

    /**
     * Return true if the pendingDrop is contained in the appliedDrops.
     */
    private boolean dropColumnIn(DropColumn pendingDrop, ChangeSet appliedDrops) {
      for (Object o : appliedDrops.getChangeSetChildren()) {
        if (o instanceof DropColumn && sameColumn(pendingDrop, (DropColumn) o)) {
          return true;
        }
      }
      return false;
    }

    /**
     * Return true if the DropHistoryTable match by base-table name.
     */
    private boolean sameHistoryTable(DropHistoryTable pendingDrop, DropHistoryTable o) {
      return pendingDrop.getBaseTable().equals(o.getBaseTable());
    }

    /**
     * Return true if the DropTable match by table name.
     */
    private boolean sameTable(DropTable pendingDrop, DropTable o) {
      return pendingDrop.getName().equals(o.getName());
    }

    /**
     * Return true if the DropColumns match by table and column name.
     */
    private boolean sameColumn(DropColumn pending, DropColumn o) {
      return pending.getColumnName().equals(o.getColumnName())
        && pending.getTableName().equals(o.getTableName());
    }

  }

  private static boolean isSuppressForever(ChangeSet next) {
    return Boolean.TRUE.equals(next.isSuppressDropsForever());
  }

}
