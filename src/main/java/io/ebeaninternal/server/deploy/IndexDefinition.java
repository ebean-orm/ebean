package io.ebeaninternal.server.deploy;

/**
 * Holds multiple column unique constraints defined for an entity.
 */
public class IndexDefinition {

  private final String[] columns;

  private final String name;

  private final boolean unique;

  /**
   * A single column index.
   */
  public IndexDefinition(String column, String name, boolean unique) {
    this.columns = new String[]{column};
    this.unique = unique;
    this.name = name;
  }

  public IndexDefinition(String[] columns, String name, boolean unique) {
    this.columns = columns;
    this.unique = unique;
    this.name = name;
  }

  /**
   * Create a unique constraint given the column names.
   */
  public IndexDefinition(String[] columns) {
    this.columns = columns;
    this.unique = true;
    this.name = null;
  }

  /**
   * Return true if this is a unique constraint.
   */
  public boolean isUnique() {
    return unique;
  }

  /**
   * Return the index name (can be null).
   */
  public String getName() {
    return name;
  }

  /**
   * Return the columns that make up this unique constraint.
   */
  public String[] getColumns() {
    return columns;
  }

}
