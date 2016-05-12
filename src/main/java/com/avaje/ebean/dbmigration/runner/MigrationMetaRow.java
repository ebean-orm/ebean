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

  private long runTime;

  /**
   * Construct for inserting into table.
   */
  MigrationMetaRow(int id, String type, String version, String comment, int checksum, String runBy, Timestamp runOn, long runTime) {
    this.id = id;
    this.type = type;
    this.version = version;
    this.checksum = checksum;
    this.comment = comment;
    this.runBy = runBy;
    this.runOn = runOn;
    this.runTime = runTime;
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
    runBy = row.getString("run_by");
    runTime = row.getLong("run_time");
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
    insert.setParameter(9, runTime);
  }

  /**
   * Return the SQL insert given the table migration meta data is stored in.
   */
  static String insertSql(String table) {
    return "insert into " + table
        + " (id, mtype, mstatus, mversion, mcomment, mchecksum, run_on, run_by, run_time)"
        + " values (?,?,?,?,?,?,?,?,?)";
  }

}
