package io.ebean.plugin;

public interface TableJoinColumnInfo {

  String getForeignDbColumn();

  String getLocalDbColumn();

}
