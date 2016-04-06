package com.avaje.ebean.dbmigration.runner;

import java.sql.Timestamp;

/**
 *
 */
public class MigrationMetaRow implements Comparable<MigrationMetaRow> {

  int id;
  String status;
  String runVersion;
  String depVersion;
  String comment;
  int checksum;
  Timestamp runOn;
  String runBy;

  @Override
  public int compareTo(MigrationMetaRow o) {
    return (id < o.id) ? -1 : ((id == o.id) ? 0 : 1);
  }


  //String sql = "insert into ebean_migration (id,status,run_version,dep_version,comment,checksum,run_on,run_by,run_ip) "+
  //    "values (?,?,?,?,?,?,?,?,?)";
}
