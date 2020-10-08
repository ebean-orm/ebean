package io.ebeaninternal.server.deploy;

import io.ebean.annotation.Platform;

/**
 * Holds multiple column unique constraints defined for an entity.
 */
public class IndexDefinition {

  private final String[] columns;
  private final String name;
  private final Platform[] platforms;
  private final boolean unique;
  private final boolean concurrent;
  private final String definition;

  /**
   * Create from Index annotation.
   */
  public IndexDefinition(String[] columns, String name, boolean unique, Platform[] platforms, boolean concurrent, String definition) {
    this.columns = columns;
    this.unique = unique;
    this.name = name;
    this.platforms = platforms;
    this.concurrent = concurrent;
    this.definition = definition;
  }

  /**
   * Create a unique constraint given the column names.
   */
  public IndexDefinition(String[] columns) {
    this.columns = columns;
    this.unique = true;
    this.name = null;
    this.platforms = null;
    this.concurrent = false;
    this.definition = null;
  }

  /**
   * Return true if this can be used as a unique constraint.
   */
  public boolean isUniqueConstraint() {
    return unique && !concurrent && noDefinition() && noColumnFormulas();
  }

  private boolean noDefinition() {
    return definition == null || definition.isEmpty();
  }

  private boolean noColumnFormulas() {
    for (String column : columns) {
      if (column.contains("(")) {
        return false;
      }
    }
    return true;
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

  /**
   * Return the platforms this index applies to.
   */
  public Platform[] getPlatforms() {
    return platforms;
  }

  /**
   * Return true if this index has the concurrent flag.
   */
  public boolean isConcurrent() {
    return concurrent;
  }

  /**
   * Return the raw definition of the index if supplied.
   */
  public String getDefinition() {
    return definition;
  }
}
