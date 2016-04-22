package com.avaje.ebean.dbmigration.runner;

import com.avaje.ebean.SqlRow;
import com.avaje.ebean.SqlUpdate;

import java.sql.Timestamp;

/**
 * Bean holding migration execution details stored in the migration table.
 */
class MigrationMetaRow {

  private int id;

  private String status;

  private String runVersion;

  private String depVersion;

  private String comment;

  private int checksum;

  private Timestamp runOn;

  private String runBy;

  /**
   * Construct for inserting into table.
   */
  MigrationMetaRow(int id, String runVersion, String priorVersion, String comment, int checksum, String runBy) {
    this.id = id;
    this.runVersion = runVersion;
    this.depVersion = priorVersion;
    this.checksum = checksum;
    this.comment = comment;
    this.runBy = runBy;
    this.runOn = new Timestamp(System.currentTimeMillis());
  }

  /**
   * Construct from the SqlRow (read from table).
   */
  MigrationMetaRow(SqlRow row) {
    id = row.getInteger("id");
    status = row.getString("status");
    runVersion = row.getString("run_version");
    depVersion = row.getString("dep_version");
    comment = row.getString("comment");
    checksum = row.getInteger("checksum");
    runOn = row.getTimestamp("run_on");
    runBy = row.getString("run_by");
  }

  public String toString() {
    return "id:" + id + " status:" + status + " runVersion:" + runVersion + " comment:" + comment + " runOn:" + runOn + " runBy:" + runBy;
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
  String getRunVersion() {
    return runVersion;
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
    insert.setParameter(2, "success");
    insert.setParameter(3, runVersion);
    insert.setParameter(4, depVersion);
    insert.setParameter(5, comment);
    insert.setParameter(6, checksum);
    insert.setParameter(7, runOn);
    insert.setParameter(8, runBy);
    insert.setParameter(9, "ip");
  }

  /**
   * Return the SQL insert given the table migration meta data is stored in.
   */
  static String insertSql(String table) {
    return "insert into " + table
        + " (id, status, run_version, dep_version, comment, checksum, run_on, run_by, run_ip)"
        + " values (?,?,?,?,?,?,?,?,?)";
  }
}
