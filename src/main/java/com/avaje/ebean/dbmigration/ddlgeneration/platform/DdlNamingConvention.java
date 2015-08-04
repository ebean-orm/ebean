package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import com.avaje.ebeaninternal.server.type.ScalarTypeBoolean;

import java.util.List;

/**
 * Naming convention used for constraint names.
 */
public class DdlNamingConvention {

  protected String pkPrefix = "pk_";
  protected String pkSuffix = "";

  protected String fkPrefix = "fk_";
  protected String fkMiddle = "_";
  protected String fkSuffix = "";

  protected String fkIndexPrefix = "ix_";
  protected String fkIndexMiddle = "_";
  protected String fkIndexSuffix = "";

  protected String uqPrefix = "uq_";
  protected String uqSuffix = "";

  protected String ckPrefix = "ck_";
  protected String ckSuffix = "";

  protected final DbNameNormalise normalise;

  public DdlNamingConvention() {
    this.normalise = new DbNameNormalise();
  }

  /**
   * Return the primary key constraint name.
   */
  public String primaryKeyName(String tableName, String[] pkColumns) {

    return pkPrefix + normalise(tableName) + pkSuffix;
  }

  /**
   * Return the foreign key constraint name given a single column foreign key.
   */
  public String foreignKeyConstraintName(String tableName, String columnName) {
    return fkPrefix + normalise(tableName) + fkMiddle + normalise(columnName) + fkSuffix;
  }

  /**
   * Return the index name associated with a foreign key constraint given a single column foreign key.
   */
  public String foreignKeyIndexName(String tableName, String[] columns) {

    String cols = columns.length == 1 ? normalise(columns[0]) : joinColumns(columns);
    return fkIndexPrefix + normalise(tableName) + fkIndexMiddle + cols + fkIndexSuffix;
  }

  private String joinColumns(String[] columns) {

    //TODO: Fix this to handle maximum constraint name limits
    StringBuilder sb = new StringBuilder(30);
    for (int i = 0; i < columns.length; i++) {
      if (i > 0) {
        sb.append("_");
      }
      sb.append(columns[i]);
    }
    return sb.toString();
  }

  /**
   * Return the unique constraint name.
   */
  public String uniqueConstraintName(String tableName, String columnName) {

    return uqPrefix + normalise(tableName) + "_" + normalise(columnName) + uqSuffix;
  }

  /**
   * Return the check constraint name.
   */
  public String checkConstraintName(String tableName, String columnName) {

    return ckPrefix + normalise(tableName) + "_" + normalise(columnName) + ckSuffix;
  }

  /**
   * Normalise the table name by trimming catalog and schema and removing any
   * quoted identifier characters (",',[,] etc).
   */
  protected String normalise(String tableName) {

    return normalise.normalise(tableName);
  }
}
