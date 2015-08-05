package com.avaje.ebean.dbmigration.ddlgeneration.platform;

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

  protected boolean lowerCaseNames = true;

  protected DdlNameNormalise normalise = new DdlNameNormalise();

  public DdlNamingConvention() {
  }

  /**
   * Return the primary key constraint name.
   */
  public String primaryKeyName(String tableName) {

    return pkPrefix + normaliseTable(tableName) + pkSuffix;
  }

  /**
   * Return the foreign key constraint name given a single column foreign key.
   */
  public String foreignKeyConstraintName(String tableName, String columnName) {
    return fkPrefix + normaliseTable(tableName) + fkMiddle + normaliseColumn(columnName) + fkSuffix;
  }

  /**
   * Return the index name associated with a foreign key constraint given a single column foreign key.
   */
  public String foreignKeyIndexName(String tableName, String[] columns) {

    String colPart;
    if (columns.length == 1) {
      colPart = normaliseColumn(columns[0]);
    } else {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i <columns.length; i++) {
        if (i > 0) {
          sb.append("_");
        }
        sb.append(normaliseColumn(columns[i]));
      }
      colPart = sb.toString();
    }
    //FIXME: apply max length
    return fkIndexPrefix + normaliseTable(tableName) + fkIndexMiddle + colPart + fkIndexSuffix;
  }

  /**
   * Return the unique constraint name.
   */
  public String uniqueConstraintName(String tableName, String columnName) {

    return uqPrefix + normaliseTable(tableName) + "_" + normaliseColumn(columnName) + uqSuffix;
  }

  /**
   * Return the check constraint name.
   */
  public String checkConstraintName(String tableName, String columnName) {

    return ckPrefix + normaliseTable(tableName) + "_" + normaliseColumn(columnName) + ckSuffix;
  }

  /**
   * Return the sequence name. If it is explicitly provided return that but
   * otherwise derive the sequence name from the table name.
   *
   * @param tableName    the table the sequence relates to
   * @param sequenceName an explicitly provided sequence name (typically null)
   * @return the sequence name to use
   */
  public String sequenceName(String tableName, String sequenceName) {

    return (sequenceName != null) ? lowerName(sequenceName) : normaliseTable(tableName) + "_seq";
  }

  /**
   * Normalise the table name by trimming catalog and schema and removing any
   * quoted identifier characters (",',[,] etc).
   */
  protected String normaliseTable(String tableName) {

    return normalise.normaliseTable(tableName);
  }

  /**
   * Normalise the column name by removing any quoted identifier characters (",',[,] etc).
   */
  protected String normaliseColumn(String tableName) {

    return normalise.normaliseColumn(tableName);
  }

  public String lowerName(String tableName) {
    if (lowerCaseNames && normalise.notQuoted(tableName)) {
      return tableName.toLowerCase();
    }
    return tableName;
  }

}
