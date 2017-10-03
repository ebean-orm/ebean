package io.ebean.plugin;


public interface TableJoinInfo {

  TableJoinColumnInfo[] columns();

  String getTable();

}
