package io.ebean.config;

/**
 * Used to normalise table and column names which means stripping out
 * quoted identifier characters and any catalog or schema prefix.
 */
public class DbConstraintNormalise {

  protected final String[] quotedIdentifiers;

  protected final boolean lowerCaseTables;

  protected final boolean lowerCaseColumns;

  public DbConstraintNormalise() {
    this(true, true);
  }

  public DbConstraintNormalise(boolean lowerCaseTables, boolean lowerCaseColumns) {
    this.lowerCaseTables = lowerCaseTables;
    this.lowerCaseColumns = lowerCaseColumns;
    this.quotedIdentifiers = new String[]{"\"", "'", "[", "]", "`"};
  }

  /**
   * Normalise the table name by trimming catalog and schema and removing any
   * quoted identifier characters (",',[,] etc).
   */
  public String normaliseTable(String tableName) {

    tableName = trimQuotes(tableName);
    int lastPeriod = tableName.lastIndexOf('.');
    if (lastPeriod > -1) {
      // trim off catalog and schema prefix
      tableName = tableName.substring(lastPeriod + 1);
    }
    if (lowerCaseTables) {
      tableName = tableName.toLowerCase();
    }
    return tableName;
  }

  /**
   * Normalise the column name by removing any quoted identifier characters and formula brackets.
   */
  public String normaliseColumn(String columnName) {
    columnName = trimBrackets(trimQuotes(columnName));
    if (lowerCaseColumns) {
      columnName = columnName.toLowerCase();
    }
    return columnName;
  }

  private String trimBrackets(String value) {
    return value.replace("(","").replace(")","");
  }

  /**
   * Trim off the platform quoted identifier quotes like [ ' and ".
   */
  public String trimQuotes(String identifier) {

    if (identifier == null) {
      return "";
    }
    // remove quoted identifier characters
    for (String quotedIdentifier : quotedIdentifiers) {
      identifier = identifier.replace(quotedIdentifier, "");
    }
    return identifier;
  }


}
