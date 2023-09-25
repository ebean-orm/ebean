package io.ebean.config;

/**
 * TableName holds catalog, schema and table name.
 *
 * @author emcgreal
 */
public final class TableName {

  /**
   * The catalog.
   */
  private String catalog;

  /**
   * The schema.
   */
  private String schema;

  /**
   * The name.
   */
  private final String name;

  /**
   * Construct with the given catalog schema and table name.
   * <p>
   * Note the catalog and schema can be null.
   * </p>
   */
  public TableName(String catalog, String schema, String name) {
    this.catalog = catalog != null ? catalog.trim() : null;
    this.schema = schema != null ? schema.trim() : null;
    this.name = name != null ? name.trim() : null;
  }

  /**
   * Construct splitting the qualifiedTableName potentially into catalog, schema
   * and name.
   * <p>
   * The qualifiedTableName can take the form of catalog.schema.tableName and is
   * split on the '.' period character. The catalog and schema are optional.
   * </p>
   *
   * @param qualifiedTableName the fully qualified table name using '.' between schema and table
   *                           name etc (with catalog and schema optional).
   */
  public TableName(String qualifiedTableName) {
    String[] split = qualifiedTableName.split("\\.");
    int len = split.length;
    if (split.length > 3) {
      String m = "Error splitting " + qualifiedTableName + ". Expecting at most 2 '.' characters";
      throw new RuntimeException(m);
    }
    if (len == 3) {
      this.catalog = split[0];
    }
    if (len >= 2) {
      this.schema = split[len - 2];
    }
    this.name = split[len - 1];
  }

  /**
   * Parse a qualifiedTableName that might include a catalog and schema and just return the table name.
   */
  public static String parse(String qualifiedTableName) {
    return new TableName(qualifiedTableName).getName();
  }

  @Override
  public String toString() {
    return getQualifiedName();
  }

  /**
   * Gets the catalog.
   *
   * @return the catalog
   */
  public String getCatalog() {
    return catalog;
  }

  /**
   * Gets the schema.
   *
   * @return the schema
   */
  public String getSchema() {
    return schema;
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the qualified name in the form catalog.schema.name.
   * <p>
   * Catalog and schema are optional.
   * </p>
   *
   * @return the qualified name
   */
  public String getQualifiedName() {
    StringBuilder buffer = new StringBuilder();
    // Add catalog
    if (catalog != null) {
      buffer.append(catalog);
    }
    // Add schema
    if (schema != null) {
      if (buffer.length() > 0) {
        buffer.append('.');
      }
      buffer.append(schema);
    }
    if (buffer.length() > 0) {
      buffer.append('.');
    }
    return buffer.append(name).toString();
  }

  /**
   * Append a catalog and schema prefix if they exist to the string builder.
   */
  public String withCatalogAndSchema(String name) {
    if (schema != null) {
      name = schema + "." + name;
    }
    if (catalog != null) {
      name = catalog + "." + name;
    }
    return name;
  }

  /**
   * Checks if is table name is valid i.e. it has at least a name.
   */
  public boolean isValid() {
    return name != null && !name.isEmpty();
  }
}
