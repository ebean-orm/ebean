package com.avaje.ebean.dbmigration.runner;

import com.avaje.ebean.SqlRow;
import com.avaje.ebean.SqlUpdate;

import java.sql.Timestamp;

/**
 * Bean holding migration execution details stored in the migration table.
 */
class MigrationMetaRow {

  private int id;

  private String type;

  private String version;

  private String comment;

  private int checksum;

  private Timestamp runOn;

  private String runBy;

  /**
   * Construct for inserting into table.
   */
  MigrationMetaRow(int id, String type, String version, String comment, int checksum, String runBy, Timestamp runOn) {
    this.id = id;
    this.type = type;
    this.version = version;
    this.checksum = checksum;
    this.comment = comment;
    this.runBy = runBy;
    this.runOn = runOn;
  }

  /**
   * Construct from the SqlRow (read from table).
   */
  MigrationMetaRow(SqlRow row) {
    id = row.getInteger("id");
    type = row.getString("mtype");
    version = row.getString("mversion");
    comment = row.getString("mcomment");
    checksum = row.getInteger("mchecksum");
    runOn = row.getTimestamp("run_on");
    runBy = row.getString("run_by");
  }

  public String toString() {
    return "id:" + id + " type:" + type + " runVersion:" + version + " comment:" + comment + " runOn:" + runOn + " runBy:" + runBy;
  }

  /**
   * Return the id for this migration.
   */
  int getId() {
    return id;
  }

  /**
   * Return the normalised version for this migration.
   */
  String getVersion() {
    return version;
  }

  /**
   * Return the checksum for this migration.
   */
  int getChecksum() {
    return checksum;
  }

  /**
   * Bind to the insert statement.
   */
  void bindInsert(SqlUpdate insert) {
    insert.setParameter(1, id);
    insert.setParameter(2, type);
    insert.setParameter(3, "SUCCESS");
    insert.setParameter(4, version);
    insert.setParameter(5, comment);
    insert.setParameter(6, checksum);
    insert.setParameter(7, runOn);
    insert.setParameter(8, runBy);
    insert.setParameter(9, "ip");
  }

  /**
   * Bind to an update statement.
   */
  public void bindUpdate(int checksum, String runBy, Timestamp runOn, SqlUpdate update) {

    this.checksum = checksum;
    this.runOn = runOn;
    this.runBy = runBy;

    update.setParameter(1, checksum);
    update.setParameter(2, runOn);
    update.setParameter(3, runBy);
    update.setParameter(4, "ip");
    update.setParameter(5, id);
  }

  /**
   * Return the SQL insert given the table migration meta data is stored in.
   */
  static String insertSql(String table) {
    return "insert into " + table
        + " (id, mtype, mstatus, mversion, mcomment, mchecksum, run_on, run_by, run_ip)"
        + " values (?,?,?,?,?,?,?,?,?)";
  }

  static String updateSql(String table) {
    return "update " + table
        + " set mchecksum = ?, run_on = ?, run_by = ?, run_ip = ?"
        + " where id = ?";
  }

}
