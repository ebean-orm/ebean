package com.avaje.ebean.dbmigration.model;

import com.avaje.ebean.dbmigration.migration.ChangeSet;
import com.avaje.ebean.dbmigration.migration.ChangeSetType;
import com.avaje.ebean.dbmigration.migration.DropColumn;
import com.avaje.ebean.dbmigration.migration.Migration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * The migrations with pending un-applied drops.
 */
public class PendingDrops {

  private final LinkedHashMap<String, Entry> map = new LinkedHashMap<String, Entry>();

  /**
   * Add a 'pending drops' changeSet for the given version.
   */
  public void add(MigrationVersion version, ChangeSet changeSet) {

    Entry entry = map.get(version.normalised());
    if (entry == null) {
      entry = new Entry(version);
      map.put(version.normalised(), entry);
    }
    entry.add(changeSet);
  }

  /**
   * Return the list of versions with pending drops.
   */
  public List<String> pendingDrops() {

    List<String> versions = new ArrayList<String>();
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
  public boolean appliedDropsFor(MigrationVersion version) {
    Entry entry = map.get(version.normalised());
    if (entry.removeDrops()) {
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
    for (ChangeSet changeSet : entry.list) {
      if (!isSuppressForever(changeSet)) {
        changeSet.setType(ChangeSetType.APPLY);
        changeSet.setDropsFor(entry.version.asString());
        migration.getChangeSet().add(changeSet);
      }
    }

    if (migration.getChangeSet().isEmpty()) {
      throw new IllegalArgumentException("The remaining pendingDrops changeSets in migration ["+pendingVersion+"] are suppressDropsForever=true and can't be applied");
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
        for (Object change : changeSet.getChangeSetChildren()) {
          if (change instanceof DropColumn) {
            DropColumn dropColumn = (DropColumn) change;
            if (Boolean.TRUE.equals(dropColumn.isWithHistory())) {
              newModel.registerPendingDropColumn(dropColumn);
            }
          }
        }
      }
    }
  }

  /**
   * Return true if there is an Entry for the given version.
   */
  boolean testContainsEntryFor(MigrationVersion version) {

    return map.containsKey(version.normalised());
  }

  static class Entry {

    final MigrationVersion version;

    final List<ChangeSet> list = new ArrayList<ChangeSet>();

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
    boolean removeDrops() {

      Iterator<ChangeSet> iterator = list.iterator();
      while (iterator.hasNext()) {
        ChangeSet next = iterator.next();
        if (!isSuppressForever(next)) {
          iterator.remove();
        }
      }

      return list.isEmpty();
    }
  }

  private static boolean isSuppressForever(ChangeSet next) {
    return Boolean.TRUE.equals(next.isSuppressDropsForever());
  }

}
