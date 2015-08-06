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

  protected int maxConstraintNameLength = 32;

  protected boolean lowerCaseNames = true;

  protected DdlNameNormalise normalise = new DdlNameNormalise();

  public DdlNamingConvention() {
  }

  /**
   * Return the primary key constraint name.
   */
  public String primaryKeyName(String tableName) {

    return maxLength(pkPrefix + normaliseTable(tableName) + pkSuffix, 0);
  }

  /**
   * Return the foreign key constraint name given a single column foreign key.
   */
  public String foreignKeyConstraintName(String tableName, String columnName, int foreignKeyCount) {
    return maxLength(fkPrefix + normaliseTable(tableName) + fkMiddle + normaliseColumn(columnName) + fkSuffix, foreignKeyCount);
  }

  /**
   * Return the index name associated with a foreign key constraint given a single column foreign key.
   */
  public String foreignKeyIndexName(String tableName, String[] columns, int indexCount) {

    String colPart;
    if (columns.length == 1) {
      colPart = normaliseColumn(columns[0]);
    } else {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < columns.length; i++) {
        if (i > 0) {
          sb.append("_");
        }
        sb.append(normaliseColumn(columns[i]));
      }
      colPart = sb.toString();
    }
    return maxLength(fkIndexPrefix + normaliseTable(tableName) + fkIndexMiddle + colPart + fkIndexSuffix, indexCount);
  }

  /**
   * Return the unique constraint name.
   */
  public String uniqueConstraintName(String tableName, String columnName, int indexCount) {

    return maxLength(uqPrefix + normaliseTable(tableName) + "_" + normaliseColumn(columnName) + uqSuffix, indexCount);
  }

  /**
   * Return the check constraint name.
   */
  public String checkConstraintName(String tableName, String columnName, int checkCount) {

    return maxLength(ckPrefix + normaliseTable(tableName) + "_" + normaliseColumn(columnName) + ckSuffix, checkCount);
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
   * Return the maximum table name length.
   * <p>
   * This is used when deriving names of intersection tables.
   * </p>
   */
  public int getMaxTableNameLength() {
    return maxConstraintNameLength;
  }

  /**
   * Apply a maximum length to the constraint name.
   */
  protected String maxLength(String constraintName, int count) {
    if (constraintName.length() < maxConstraintNameLength) {
      return constraintName;
    }
    // add the count to ensure the constraint name is unique
    // (relying on the prefix having the table name to be globally unique)
    return constraintName.substring(0, maxConstraintNameLength - 3) + "_" + count;
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
