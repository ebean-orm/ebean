package com.avaje.ebean.config;

/**
 * Naming convention used for constraint names.
 * <p>
 * Note that these constraint names are trimmed in the PlatformDdl which can be overridden
 * but provides a decent default implementation.
 * </p>
 */
public class DbConstraintNaming {

  /**
   * Defines how constraint names are shortened if required based on platform limitations.
   */
  public interface MaxLength {

    /**
     * Truncate or shorten the constraint name to support DB platform limitations.
     * <p>
     * There is a default implementation of this which is used if an implementation is
     * not specified.
     * </p>
     */
    String maxLength(String constraintName, int count);
  }

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

  protected MaxLength maxLength;

  protected DbConstraintNormalise normalise = new DbConstraintNormalise();

  public DbConstraintNaming() {
  }

  /**
   * Return the MaxLength implementation used to truncate/shorten db constraint names as necessary.
   */
  public MaxLength getMaxLength() {
    return maxLength;
  }

  /**
   * Set the MaxLength implementation used to truncate/shorten db constraint names as necessary.
   */
  public void setMaxLength(MaxLength maxLength) {
    this.maxLength = maxLength;
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

    String colPart = joinColumnNames(columns);
    return fkIndexPrefix + normaliseTable(tableName) + fkIndexMiddle + colPart + fkIndexSuffix;
  }

  public String foreignKeyIndexName(String tableName, String column) {

    String colPart = normaliseTable(column);
    return fkIndexPrefix + normaliseTable(tableName) + fkIndexMiddle + colPart + fkIndexSuffix;
  }

  /**
   * Join the column names together with underscores.
   */
  protected String joinColumnNames(String[] columns) {

    if (columns.length == 1) {
      return normaliseColumn(columns[0]);
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < columns.length; i++) {
      if (i > 0) {
        sb.append("_");
      }
      sb.append(normaliseColumn(columns[i]));
    }
    return sb.toString();
  }

  /**
   * Return the unique constraint name.
   */
  public String uniqueConstraintName(String tableName, String columnName) {

    return uqPrefix + normaliseTable(tableName) + "_" + normaliseColumn(columnName) + uqSuffix;
  }

  /**
   * Return the unique constraint name.
   */
  public String uniqueConstraintName(String tableName, String[] columns) {

    String colPart = joinColumnNames(columns);
    return uqPrefix + normaliseTable(tableName) + "_" + colPart + uqSuffix;
  }

  /**
   * Return the check constraint name.
   */
  public String checkConstraintName(String tableName, String columnName) {

    return ckPrefix + normaliseTable(tableName) + "_" + normaliseColumn(columnName) + ckSuffix;
  }

  /**
   * Normalise the table name by trimming catalog and schema and removing any
   * quoted identifier characters (",',[,] etc).
   */
  public String normaliseTable(String tableName) {

    return normalise.normaliseTable(tableName);
  }

  /**
   * Normalise the column name by removing any quoted identifier characters (",',[,] etc).
   */
  public String normaliseColumn(String tableName) {

    return normalise.normaliseColumn(tableName);
  }

  /**
   * Lower case the table or column name checking for quoted identifiers.
   */
  public String lowerName(String tableName) {
    if (lowerCaseNames && normalise.notQuoted(tableName)) {
      return tableName.toLowerCase();
    }
    return tableName;
  }

}
