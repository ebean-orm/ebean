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
      versions.add(value.version.asString());
    }
    return versions;
  }

  /**
   * Remove the pending drops for a version (as they have been applied).
   */
  public void remove(MigrationVersion version) {
    map.remove(version.normalised());
  }

  /**
   * Return true if there are no pending drops.
   */
  public boolean isEmpty() {
    return map.isEmpty();
  }

  /**
   * Return the migration for the pending drops from a version.
   * <p>
   * The value of version can be "next" to find the first un-applied pending drops.
   * </p>
   */
  public Migration migrationForVersion(String pendingVersion) {

    Entry entry = getChangeSets(pendingVersion);

    Migration migration = new Migration();
    for (ChangeSet changeSet : entry.list) {
      changeSet.setType(ChangeSetType.APPLY);
      changeSet.setDropsFor(entry.version.asString());
      migration.getChangeSet().add(changeSet);
    }

    return migration;
  }

  private Entry getChangeSets(String pendingVersion) {

    if ("next".equalsIgnoreCase(pendingVersion)) {
      Iterator<Entry> it = map.values().iterator();
      if (it.hasNext()) {
        Entry first = it.next();
        it.remove();
        return first;
      }
    } else {
      Entry remove = map.remove(MigrationVersion.parse(pendingVersion).normalised());
      if (remove != null) {
        return remove;
      }
    }
    throw new IllegalArgumentException("No pending changeSets for version [" + pendingVersion + "] found");
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

  static class Entry {

    final MigrationVersion version;

    final List<ChangeSet> list = new ArrayList<ChangeSet>();

    Entry(MigrationVersion version) {
      this.version = version;
    }

    void add(ChangeSet changeSet) {
      list.add(changeSet);
    }
  }

}
