package io.ebean.dbmigration.ddlgeneration;

import io.ebean.dbmigration.migration.AddColumn;
import io.ebean.dbmigration.migration.ChangeSet;
import io.ebean.dbmigration.migration.CreateTable;
import io.ebean.dbmigration.migration.DropColumn;
import io.ebean.dbmigration.migration.Migration;
import io.ebean.dbmigration.migrationreader.MigrationXmlReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.List;

/**
 * Helper for testing to return some basic migration objects.
 */
public class Helper {

  static Migration migration = MigrationXmlReader.read("/container/test-create-table.xml");

  static Migration alterTableMigration = MigrationXmlReader.read("/container/test-alter-table.xml");

  static ChangeSet changeSet;

  static List<Object> changeSetChildren;

  static ChangeSet alterTableChangeSet;

  static List<Object> alterTableChangeSetChildren;

  static {
    List<ChangeSet> changeSets = migration.getChangeSet();
    changeSet = changeSets.get(0);
    changeSetChildren = changeSet.getChangeSetChildren();

    alterTableChangeSet = alterTableMigration.getChangeSet().get(0);
    alterTableChangeSetChildren = alterTableChangeSet.getChangeSetChildren();
  }

  public static ChangeSet getChangeSet() {
    return changeSet;
  }

  public static CreateTable getCreateTable() {
    return (CreateTable) changeSetChildren.get(0);
  }

  public static AddColumn getAddColumn() {
    return (AddColumn) changeSetChildren.get(1);
  }

  public static AddColumn getAlterTableAddColumn() {
    return (AddColumn) alterTableChangeSetChildren.get(0);
  }

  public static DropColumn getDropColumn() {
    return (DropColumn) changeSetChildren.get(2);
  }

  public static String asText(Object instance, String relativePath) throws IOException {
    InputStream is = instance.getClass().getResourceAsStream(relativePath);
    if (is == null) {
      throw new IllegalArgumentException("resource " + relativePath + " not found");
    }
    return asText(is);
  }

  public static String asText(InputStream in) throws IOException {

    try {
      InputStreamReader reader = new InputStreamReader(in);

      LineNumberReader lineNumberReader = new LineNumberReader(reader);

      StringBuilder builder = new StringBuilder(400);
      String line;
      while ((line = lineNumberReader.readLine()) != null) {
        builder.append(line).append("\n");
      }
      return builder.toString();

    } finally {
      in.close();
    }
  }

}
